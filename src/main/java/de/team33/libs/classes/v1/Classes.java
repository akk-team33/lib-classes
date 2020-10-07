package de.team33.libs.classes.v1;

import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Utility for dealing with classes.
 */
public class Classes {

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
     * Streams a given class that may be {@code null}.
     * In that case, the resulting {@link Stream} is empty, otherwise it contains exactly the one given class.
     */
    public static Stream<Class<?>> streamOptional(final Class<?> subject) {
        return (null == subject) ? Stream.empty() : Stream.of(subject);
    }

    /**
     * Streams the direct {@link Class#getSuperclass() superclass} of a given {@link Class}.
     * The result is either an empty {@link Stream} or a {@link Stream} containing exactly one element.
     */
    public static Stream<Class<?>> streamSuperClass(final Class<?> subject) {
        return streamOptional(subject.getSuperclass());
    }

    /**
     * Streams the direct {@link Class#getInterfaces() interfaces} of a given {@link Class}.
     */
    public static Stream<Class<?>> streamInterfaces(final Class<?> subject) {
        return Stream.of(subject.getInterfaces());
    }

    /**
     * Streams, in a single step, all the direct {@link Class#getInterfaces() interfaces} and, if any, the direct
     * {@link Class#getSuperclass() superclass} of a given {@link Class}.
     */
    public static Stream<Class<?>> streamSuperior(final Class<?> subject) {
        return Stream.concat(streamInterfaces(subject), streamSuperClass(subject));
    }

    /**
     * Streams the linear descent of a given {@link Class}, made up of the linear descent of its
     * {@link Class#getSuperclass() superclass}, and the {@link Class} itself.
     *
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

    private static Stream<Class<?>> streamBroadDescent(final Stream<Class<?>> subjects) {
        return subjects.map(Classes::streamLineageHierarchy)
                       .reduce(Stream::concat)
                       .orElseGet(Stream::empty);
    }

    /**
     * Streams a {@link Class} hierarchy. This method includes the given {@link Class}, its
     * {@linkplain Class#getSuperclass() superclasses} and its {@linkplain Class#getInterfaces() superinterfaces}.
     *
     * @see #streamOptional(Class)
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

    private static boolean isHierarchical(final Class<?> superClass, final Stream<Class<?>> subClasses) {
        return subClasses.anyMatch(subClass -> isHierarchical(superClass, subClass));
    }

    public static boolean isHierarchical(final Class<?> superClass, final Class<?> subClass) {
        return superClass.equals(subClass) || isHierarchical(superClass, Stream.concat(
                Stream.of(subClass.getInterfaces()),
                streamOptional(subClass.getSuperclass())
        ));
    }

    /**
     * Provides streaming methods from {@link Classes} as {@link Function}s.
     */
    public interface Streaming extends Function<Class<?>, Stream<Class<?>>> {

        /**
         * Encapsulates {@link #streamOptional(Class)} as {@link Function}
         */
        Streaming OPTIONAL = Classes::streamOptional;

        /**
         * Encapsulates {@link #streamSuperClass(Class)} as {@link Function}
         */
        Streaming SUPER_CLASS = Classes::streamSuperClass;

        /**
         * Encapsulates {@link #streamInterfaces(Class)} as a {@link Function}
         */
        Streaming INTERFACES = Classes::streamInterfaces;

        /**
         * Encapsulates {@link #streamSuperior(Class)} as a {@link Function}
         */
        Streaming SUPERIOR = Classes::streamSuperior;

        Streaming LINEAR_DESCENT = Classes::streamLinearDescent;

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

        final int from(final Class<?> subClass) {
            return superClass.equals(subClass) ? 0 : 1 + from(superClasses.apply(subClass));
        }

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        private int from(final Stream<Class<?>> subClasses) {
            return subClasses.filter(subClass -> isHierarchical(superClass, subClass))
                             .map(this::from)
                             .reduce(LIMIT, Math::min);
        }
    }
}
