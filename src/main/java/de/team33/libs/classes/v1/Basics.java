package de.team33.libs.classes.v1;

import java.util.stream.Stream;


class Basics {

    static <E> Stream<E> streamOfNullable(final E subject) {
        return (null == subject) ? Stream.empty() : Stream.of(subject);
    }

    static Stream<Class<?>> streamOfSuperior(final Class<?> subject) {
        return (null == subject)
                ? Stream.empty()
                : Stream.concat(Stream.of(subject.getInterfaces()), streamOfNullable(subject.getSuperclass()));
    }
}
