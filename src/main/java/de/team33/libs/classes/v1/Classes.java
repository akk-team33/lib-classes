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
    public static Stream<Class<?>> streamOf(final Class<?> subject) {
        return (null == subject) ? Stream.empty() : Stream.of(subject);
    }

    /**
     * Streams a {@link Class} hierarchy. This method focuses on a given {@link Class} and its
     * {@linkplain Class#getSuperclass() superclasses}, but not on its
     * {@linkplain Class#getInterfaces() superinterfaces}.
     *
     * @see #streamOf(Class)
     * @see #wideStreamOf(Class)
     */
    public static Stream<Class<?>> deepStreamOf(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : Stream.concat(deepStreamOf(subject.getSuperclass()), Stream.of(subject));
    }

    /**
     * Streams a {@link Class} hierarchy. This method includes the given {@link Class}, its
     * {@linkplain Class#getSuperclass() superclasses} and its {@linkplain Class#getInterfaces() superinterfaces}.
     *
     * @see #streamOf(Class)
     * @see #deepStreamOf(Class)
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
                streamOf(subClass.getSuperclass())
        ));
    }

    public interface Streaming extends Function<Class<?>, Stream<Class<?>>> {

        /**
         * Streams a single {@link Class} that is probably {@code null}.
         * The result is either an empty {@link Stream} or a {@link Stream} containing exactly one element.
         */
        Streaming OPTIONAL = subject -> (null == subject) ? Stream.empty() : Stream.of(subject);

        /**
         * Streams the direct {@link Class#getSuperclass() superclass} of a given {@link Class}.
         * The result is either an empty {@link Stream} or a {@link Stream} containing exactly one element.
         */
        Streaming SUPER_CLASS = subject -> OPTIONAL.apply(subject.getSuperclass());

        /**
         * Streams the direct {@link Class#getInterfaces() interfaces} of a given {@link Class}.
         */
        Streaming INTERFACES = subject -> Stream.of(subject.getInterfaces());

        /**
         * Streams the direct {@link Class#getSuperclass() superclass} and the direct
         * {@link Class#getInterfaces() interfaces} of a given {@link Class}.
         */
        Streaming SUPERIORS = subject -> Stream.concat(INTERFACES.apply(subject), SUPER_CLASS.apply(subject));

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
