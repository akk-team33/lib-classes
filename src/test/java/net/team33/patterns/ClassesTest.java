package net.team33.patterns;

import org.junit.Assert;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.stream.Stream;

public class ClassesTest {

    private static final List<Class<?>> CLASSES = Arrays.asList(
            String.class, StringBuilder.class, Serializable.class, Object.class, List.class, Set.class,
            Collection.class, Map.class, Iterable.class, CharSequence.class, Character.class, Boolean.class,
            Byte.class, Integer.class, Number.class, Double.class, Double.TYPE, Integer.TYPE,
            BigInteger.class, BigDecimal.class, ArrayList.class, AbstractMap.class, ArrayBlockingQueue.class
    );
    private static final Object[][] RESULTS = {
            {Object.class, Object.class, 0},
            {Object.class, String.class, 1},
            {Object.class, Serializable.class, 1},
            {Object.class, List.class, 1},
            {Object.class, Set.class, 1},
            {Object.class, Collection.class, 1},
            {Object.class, Map.class, 1},
            {Object.class, Iterable.class, 1},
            {Object.class, CharSequence.class, 1},
            {Object.class, Character.class, 1},
            {Object.class, Boolean.class, 1},
            {Object.class, Number.class, 1},
            {Object.class, AbstractMap.class, 1},
            {Object.class, Byte.class, 2},
            {Object.class, StringBuilder.class, 2},
            {Object.class, Integer.class, 2},
            {Object.class, BigInteger.class, 2},
            {Object.class, Double.class, 2},
            {Object.class, BigInteger.class, 2},
            {Object.class, BigDecimal.class, 2},
            {Object.class, ArrayList.class, 2},
            {Object.class, ArrayBlockingQueue.class, 2},
            {Serializable.class, Serializable.class, 0},
            {Serializable.class, String.class, 1},
            {Serializable.class, StringBuilder.class, 1},
            {Serializable.class, Character.class, 1},
            {Serializable.class, Boolean.class, 1},
            {Serializable.class, Number.class, 1},
            {Serializable.class, ArrayList.class, 1},
            {Serializable.class, ArrayBlockingQueue.class, 1},
            {Serializable.class, Byte.class, 2},
            {Serializable.class, Integer.class, 2},
            {Serializable.class, BigInteger.class, 2},
            {Serializable.class, Double.class, 2},
            {Serializable.class, BigInteger.class, 2},
            {Serializable.class, BigDecimal.class, 2},
            {Number.class, Number.class, 0},
            {Number.class, Byte.class, 1},
            {Number.class, Integer.class, 1},
            {Number.class, BigInteger.class, 1},
            {Number.class, Double.class, 1},
            {Number.class, BigInteger.class, 1},
            {Number.class, BigDecimal.class, 1},
            {Iterable.class, List.class, 2},
            {Iterable.class, Set.class, 2},
            {Iterable.class, Collection.class, 1},
            {Iterable.class, Iterable.class, 0},
            {Iterable.class, ArrayList.class, 3},
            {Iterable.class, ArrayBlockingQueue.class, 4},
            {Collection.class, List.class, 1},
            {Collection.class, Set.class, 1},
            {Collection.class, Collection.class, 0},
            {Collection.class, ArrayList.class, 2},
            {Collection.class, ArrayBlockingQueue.class, 3},
            {CharSequence.class, String.class, 1},
            {CharSequence.class, StringBuilder.class, 1},
            {CharSequence.class, CharSequence.class, 0},
            {List.class, List.class, 0},
            {List.class, ArrayList.class, 1},
            {Set.class, Set.class, 0},
            {Map.class, Map.class, 0},
            {Map.class, AbstractMap.class, 1},
            {Character.class, Character.class, 0},
            {Boolean.class, Boolean.class, 0},
            {Byte.class, Byte.class, 0},
            {Integer.class, Integer.class, 0},
            {BigInteger.class, BigInteger.class, 0},
            {BigInteger.class, BigInteger.class, 0},
            {String.class, String.class, 0},
            {StringBuilder.class, StringBuilder.class, 0},
            {Double.class, Double.class, 0},
            {double.class, double.class, 0},
            {int.class, int.class, 0},
            {BigInteger.class, BigInteger.class, 0},
            {BigDecimal.class, BigDecimal.class, 0},
            {ArrayList.class, ArrayList.class, 0},
            {AbstractMap.class, AbstractMap.class, 0},
            {ArrayBlockingQueue.class, ArrayBlockingQueue.class, 0}
    };

    @Test
    public final void any() {
        CLASSES.forEach(left -> CLASSES.forEach(right -> {
            final int expected = Stream.of(RESULTS)
                    .filter(results -> left == results[0] && right == results[1])
                    .map(results -> (Integer) (results[2]))
                    .findAny()
                    .orElse(-1);
            try {
                final int distance = Classes.distance(left, right);
                Assert.assertTrue(left.isAssignableFrom(right));
                Assert.assertEquals(expected, distance);
            } catch (final Exception ignored) {
                Assert.assertFalse(left.isAssignableFrom(right));
                Assert.assertEquals(-1, expected);
            }
        }));
    }
}