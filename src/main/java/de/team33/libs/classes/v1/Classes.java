package de.team33.libs.classes.v1;

import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Utility for dealing with classes.
 */
@SuppressWarnings("WeakerAccess")
public class Classes {

    private static final String NO_LINEAGE =
            "there is no proper lineage relationship from <%s> as superclass to <%s> as subclass";

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
     * Determines the distance between a class {@code <subClass>} and a superordinate class from its lineage hierarchy
     * {@code <superClass>}, whereby the distance between a class and itself is always 0.
     *
     * The distance between a class and its direct {@link Class#getSuperclass() superclass} or one of its direct
     * {@link Class#getInterfaces() interfaces} is always 1 and so on.
     *
     * @throws IllegalArgumentException if {@code <superClass>} ist not a superordinate class from the lineage
     *                                  hierarchy of {@code <subClass>} or at least the {@code <subClass>} itself.
     * @throws NullPointerException     if one of the given Arguments is {@code null}.
     */
    public static int distance(final Class<?> superClass, final Class<?> subClass) {
        return Distance.of(superClass).from(subClass);
    }

    /**
     * Streams, in a single step, all the direct {@link Class#getInterfaces() interfaces} and, if any, the direct
     * {@link Class#getSuperclass() superclass} of a given {@link Class}.
     */
    public static Stream<Class<?>> superior(final Class<?> subject) {
        return Basics.superior(subject);
    }

    /**
     * Streams the lineage classes of a given class.
     * In particular results in a {@link Stream} that is composed of the lineage classes of the
     * ({@link Class#getSuperclass() superclass} and finally the mentioned class itself.
     */
    public static Stream<Class<?>> lineageClasses(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : Stream.concat(lineageClasses(subject.getSuperclass()), Stream.of(subject));
    }

    /**
     * Streams the lineage hierarchy of a given class.
     * In particular results in a {@link Stream} that is composed of the lineage hierarchy of any superordinate classes
     * ({@link Class#getSuperclass() superclass} and {@link Class#getInterfaces() interfaces}) and finally the
     * mentioned class itself.
     */
    public static Stream<Class<?>> lineageHierarchy(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : streamLineageHierarchyIndistinct(subject).distinct();
    }

    private static Stream<Class<?>> streamLineageHierarchyIndistinct(final Class<?> subject) {
        return Stream.concat(superior(subject).map(Classes::streamLineageHierarchyIndistinct)
                                              .reduce(Stream::concat)
                                              .orElseGet(Stream::empty),
                             Stream.of(subject));
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
        Streaming SUPER_CLASS = subject -> Basics.stream(subject.getSuperclass());

        /**
         * For the sake of completeness and convenience: a {@link Function} to treat the direct
         * {@link Class#getInterfaces() interfaces} of a given {@link Class} as a {@link Stream}.
         */
        Streaming INTERFACES = subject -> Stream.of(subject.getInterfaces());

        /**
         * Encapsulates {@link #superior(Class)} as a {@link Function}
         */
        Streaming SUPERIOR = Classes::superior;

        /**
         * Encapsulates {@link #lineageClasses(Class)} as a {@link Function}
         */
        Streaming LINEAGE_CLASSES = Classes::lineageClasses;

        /**
         * Encapsulates {@link #lineageHierarchy(Class)} as a {@link Function}
         */
        Streaming LINEAGE_HIERARCHY = Classes::lineageHierarchy;
    }

    private static final class Distance {

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
