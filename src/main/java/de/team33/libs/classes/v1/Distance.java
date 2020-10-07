package de.team33.libs.classes.v1;

import java.util.function.Function;
import java.util.stream.Stream;


class Distance {

    private static final Function<Class<?>, Stream<Class<?>>> STRAIGHT = sub -> Classes.streamOptional(sub.getSuperclass());
    private static final Function<Class<?>, Stream<Class<?>>> WIDE = sub -> Stream.concat(Stream.of(sub.getInterfaces()), STRAIGHT.apply(sub));

    private final Class<?> superClass;
    private final Function<Class<?>, Stream<Class<?>>> superClasses;

    private Distance(final Class<?> superClass, final Function<Class<?>, Stream<Class<?>>> superClasses) {
        this.superClass = superClass;
        this.superClasses = superClasses;
    }

    static Distance of(final Class<?> superClass) {
        return new Distance(superClass, superClass.isInterface() ? WIDE : STRAIGHT);
    }

    final int from(final Class<?> subClass) {
        try {
            return (subClass == superClass) ? 0 : (1 + from(superClasses.apply(subClass)));
        } catch (InternalException e) {
            throw new IllegalArgumentException(String.format(
                    "<%s> is not a derivative of <%s>", subClass.getCanonicalName(), superClass.getCanonicalName()
            ));
        }
    }

    private int from(final Stream<Class<?>> subClasses) throws InternalException {
        return subClasses
                .filter(sub -> Classes.isHierarchical(superClass, sub))
                .map(this::from)
                .reduce(Math::min)
                .orElseThrow(InternalException::new);
    }

    private static class InternalException extends Exception {
    }
}
