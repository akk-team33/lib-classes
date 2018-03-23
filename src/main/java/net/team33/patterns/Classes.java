package net.team33.patterns;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import java.util.stream.Stream;

public final class Classes {

    private static final Map<Class<?>, Map<Class<?>, Integer>> DISTANCES = new ConcurrentHashMap<>(0);

    private Classes() {
    }

    public static int distance(final Class<?> superClass, final Class<?> subClass) {
        final Map<Class<?>, Integer> distances = Optional.ofNullable(DISTANCES.get(superClass)).orElseGet(() -> {
            final Map<Class<?>, Integer> result = new ConcurrentHashMap<>(0);
            DISTANCES.put(superClass, result);
            return result;
        });
        return Optional.ofNullable(distances.get(subClass)).orElseGet(() -> {
            final int result = superClass.equals(subClass)
                    ? 0
                    : (1 + distance(superClass, superClasses(subClass), () -> new IllegalArgumentException(
                    String.format("Cannot calculate distance between <%s> and <%s>", superClass, subClass))));
            distances.put(subClass, result);
            return result;
        });
    }

    private static int distance(final Class<?> superClass, final Stream<Class<?>> subClasses,
                                final Supplier<IllegalArgumentException> newException) {
        return subClasses
                .filter(superClass::isAssignableFrom)
                .map(subClass -> distance(superClass, subClass))
                .reduce((left, right) -> (right < left) ? right : left)
                .orElseThrow(newException);
    }

    private static Stream<Class<?>> superClasses(final Class<?> subClass) {
        return Stream.concat(
                Optional.ofNullable(subClass.isInterface() ? Object.class : subClass.getSuperclass())
                        .map(Stream::<Class<?>>of)
                        .orElse(Stream.empty()),
                Stream.of(subClass.getInterfaces())
        );
    }
}
