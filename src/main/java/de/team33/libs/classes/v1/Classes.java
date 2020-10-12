package de.team33.libs.classes.v1;

import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Utility for dealing with classes.
 */
@SuppressWarnings("WeakerAccess")
public class Classes {

    private static final String NO_LINEAGE =
            "there is no proper lineage relationship from <%s> as superclass to <%s> as subclass";

    private static <E> Stream<E> stream(final E subject) {
        return (null == subject) ? Stream.empty() : Stream.of(subject);
    }

    /**
     * Determines whether there is an (possibly indirect but) explicit lineage relationship between two classes.
     * <p>
     * <em>Note: Any interface {@link Class#isAssignableFrom(Class) can be assigned to} the class Object, for example,
     * but there is no explicit lineage relationship between {@link Object} and any interface.</em>
     *
     * @throws NullPointerException if one of the given Arguments is {@code null}.
     */
    public static boolean isLineage(final Class<?> superClass, final Class<?> subClass) {
        return (superClass.isInterface() || !subClass.isInterface()) && superClass.isAssignableFrom(subClass);
    }

    /**
     * Determines the distance of a given class {@code <subject>} from one of its superclasses or interfaces
     * {@code <superClass>}, where the distance of a class to itself is always 0.
     *
     * @throws IllegalArgumentException if {@code <subject>} ist not a derivative of {@code <superClass>}.
     * @throws NullPointerException     if one of the given Arguments is {@code null}.
     */
    public static int distance(final Class<?> subject, final Class<?> superClass) {
        return Distance.of(superClass).from(subject);
    }

    /**
     * Streams, in a single step, all the direct {@link Class#getInterfaces() interfaces} and, if any, the direct
     * {@link Class#getSuperclass() superclass} of a given {@link Class}.
     */
    public static Stream<Class<?>> streamSuperior(final Class<?> subject) {
        return Stream.concat(Stream.of(subject.getInterfaces()), stream(subject.getSuperclass()));
    }

    /**
     * Streams the linear descent of a given {@link Class}, made up of the linear descent of its
     * {@link Class#getSuperclass() superclass}, and the {@link Class} itself.
     * <p>
     * The Result will be an empty {@link Stream} if {@code subject} is {@code null}.
     */
    public static Stream<Class<?>> streamLinearDescent(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : Stream.concat(streamLinearDescent(subject.getSuperclass()), Stream.of(subject));
    }

    /**
     * Streams the lineage hierarchy of a given class.
     * In particular results in a {@link Stream} that is composed of the lineage hierarchy of any superordinate classes
     * ({@link Class#getSuperclass() superclass} and {@link Class#getInterfaces() interfaces}) and finally the
     * mentioned class itself.
     */
    public static Stream<Class<?>> streamLineageHierarchy(final Class<?> subject) {
        return streamLineageHierarchyIndistinct(subject).distinct();
    }

    private static Stream<Class<?>> streamLineageHierarchyIndistinct(final Class<?> subject) {
        return Stream.concat(streamSuperior(subject).map(Classes::streamLineageHierarchyIndistinct)
                                                    .reduce(Stream::concat)
                                                    .orElseGet(Stream::empty),
                             Stream.of(subject));
    }

    /**
     * Streams a {@link Class} hierarchy. This method includes the given {@link Class}, its
     * {@linkplain Class#getSuperclass() superclasses} and its {@linkplain Class#getInterfaces() superinterfaces}.
     *
     * @see #streamLinearDescent(Class)
     */
    public static Stream<Class<?>> wideStreamOf(final Class<?> subject) {
        return broad(subject).distinct();
    }

    private static Stream<Class<?>> broad(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : broad(subject.getInterfaces(), subject.getSuperclass(), subject);
    }

    private static Stream<Class<?>> broad(final Class<?>[] interfaces,
                                          final Class<?> superclass,
                                          final Class<?> subject) {
        return Stream.concat(broad(interfaces), Stream.concat(broad(superclass), Stream.of(subject)));
    }

    private static Stream<Class<?>> broad(final Class<?>[] subjects) {
        return Stream.of(subjects).map(Classes::broad).reduce(Stream::concat).orElseGet(Stream::empty);
    }

    /**
     * Provides streaming methods from {@link Classes} as predefined {@link Function}s.
     */
    public interface Streaming extends Function<Class<?>, Stream<Class<?>>> {

        /**
         * For the sake of completeness and convenience: a {@link Function} to treat the direct
         * {@link Class#getSuperclass() superclass} of a given {@link Class} as a {@link Stream}.
         * <p>
         * The result will be an empty {@link Stream} if the given {@link Class} has no {@link Class#getSuperclass()
         * superclass}. Otherwise the result is a {@link Stream} consisting of exactly one element, namely the
         * requested {@link Class#getSuperclass() superclass}.
         */
        Streaming SUPER_CLASS = subject -> stream(subject.getSuperclass());

        /**
         * For the sake of completeness and convenience: a {@link Function} to treat the direct
         * {@link Class#getInterfaces() interfaces} of a given {@link Class} as a {@link Stream}.
         */
        Streaming INTERFACES = subject -> Stream.of(subject.getInterfaces());

        /**
         * Encapsulates {@link #streamSuperior(Class)} as a {@link Function}
         */
        Streaming SUPERIOR = Classes::streamSuperior;

        /**
         * Encapsulates {@link #streamLinearDescent(Class)} as a {@link Function}
         */
        Streaming LINEAR_DESCENT = Classes::streamLinearDescent;

        /**
         * Encapsulates {@link #streamLineageHierarchy(Class)} as a {@link Function}
         */
        Streaming LINEAGE_HIERARCHY = Classes::streamLineageHierarchy;
    }

    private static class Distance {

        private static final int LIMIT = Short.MAX_VALUE;

        private final Class<?> superClass;
        private final Function<Class<?>, Stream<Class<?>>> superClasses;

        private Distance(final Class<?> superClass, final Function<Class<?>, Stream<Class<?>>> superClasses) {
            this.superClass = superClass;
            this.superClasses = superClasses;
        }

        static Distance of(final Class<?> superClass) {
            if (superClass.isInterface())
                return new Distance(superClass, Distance::superClassesOf);
            else
                return new Distance(superClass, Distance::superClassOf);
        }

        private static <E> Stream<E> streamOf(final E nullable) {
            return (null == nullable) ? Stream.empty() : Stream.of(nullable);
        }

        private static Stream<Class<?>> superClassOf(final Class<?> aClass) {
            return streamOf(aClass.getSuperclass());
        }

        private static Stream<Class<?>> superClassesOf(final Class<?> aClass) {
            return Stream.concat(Stream.of(aClass.getInterfaces()), superClassOf(aClass));
        }

        private int from(final Class<?> subClass) {
            try {
                return superClass.equals(subClass) ? 0 : 1 + from(superClasses.apply(subClass));
            } catch (final NoLineageException e) {
                throw new IllegalArgumentException(String.format(NO_LINEAGE, superClass, subClass), e);
            }
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        private int from(final Stream<Class<?>> subClasses) {
            return subClasses.filter(subClass -> isLineage(superClass, subClass))
                             .map(this::from)
                             .reduce(Math::min)
                             .orElseThrow(NoLineageException::new);
        }

        private static class NoLineageException extends RuntimeException {
        }
    }
}
