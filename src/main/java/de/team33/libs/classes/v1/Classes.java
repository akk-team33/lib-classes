package de.team33.libs.classes.v1;

import java.util.function.Function;
import java.util.stream.Stream;


/**
 * Utility for dealing with classes.
 */
public class Classes {

    private static final Function<Class<?>, Stream<Class<?>>> DEEP = Classes::superClass;
    private static final Function<Class<?>, Stream<Class<?>>> WIDE = Classes::superClasses;

    /**
     * Determines the distance of a given class {@code <subject>} from one of its superclasses or interfaces
     * {@code <superClass>}, where the distance of a class to itself is always 0.
     *
     * @throws IllegalArgumentException if {@code <subject>} ist not a derivative of {@code <superClass>}.
     * @throws NullPointerException     if one of the given Arguments is {@code null}.
     */
    public static int distance(final Class<?> subject, final Class<?> superClass) {
        return new Distance(superClass, superClass.isInterface() ? WIDE : DEEP).from(subject);
    }

    private static Stream<Class<?>> superClass(final Class<?> subClass) {
        return streamOf(subClass.getSuperclass());
    }

    private static Stream<Class<?>> superClasses(final Class<?> subClass) {
        return Stream.concat(Stream.of(subClass.getInterfaces()), superClass(subClass));
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
        return (null == subject) ? Stream.empty() :
                Stream.concat(deepStreamOf(subject.getSuperclass()), Stream.of(subject));
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
        return (null == subject) ? Stream.empty() :
                broad(subject.getInterfaces(), subject.getSuperclass(), subject);
    }

    private static Stream<Class<?>> broad(final Class<?>[] interfaces,
                                          final Class<?> superclass,
                                          final Class<?> subject) {
        return Stream.concat(broad(interfaces), Stream.concat(broad(superclass), Stream.of(subject)));
    }

    private static Stream<Class<?>> broad(final Class<?>[] subjects) {
        return Stream.of(subjects).map(Classes::broad).reduce(Stream::concat).orElseGet(Stream::empty);
    }
}
