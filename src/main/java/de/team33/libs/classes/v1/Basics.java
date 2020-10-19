package de.team33.libs.classes.v1;

import java.util.stream.Stream;


class Basics {

    static <E> Stream<E> stream(final E subject) {
        return (null == subject) ? Stream.empty() : Stream.of(subject);
    }

    static Stream<Class<?>> superior(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : Stream.concat(Stream.of(subject.getInterfaces()), stream(subject.getSuperclass()));
    }
}
