package de.team33.test.classes.v1;

import de.team33.libs.lazy.v1.Lazy;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class CacheTest {

    private static final int LOOP_LIMIT = 100000;
    private static final int INT_BOUND = 100;
    private static final int THREAD_LIMIT = 1000;

    private static Map<Integer, Recursive> CACHE = new ConcurrentHashMap<>(0);

    private static Recursive recursive(final int key) {
        return CACHE.computeIfAbsent(key, Recursive::new);
    }

    @SuppressWarnings("ConstantConditions")
    @BeforeClass
    public static void beforeClass() {
        // Just in case someone has changed the constants in such a way that tests can no longer work properly ...
        assertTrue(String.format("LOOP_LIMIT (%d) should be significantly larger than INT_BOUND (%d)",
                                 LOOP_LIMIT, INT_BOUND),
                   100 <= LOOP_LIMIT / INT_BOUND);
        assertTrue(String.format("THREAD_LIMIT (%d) should be larger than INT_BOUND (%d)",
                                 THREAD_LIMIT, INT_BOUND),
                   1 <= THREAD_LIMIT / INT_BOUND);
    }

    @Test
    public final void testRecursive() {
        final Random random = new Random();
        Stream.generate(() -> random.nextInt(INT_BOUND))
              .limit(LOOP_LIMIT)
              .forEach(key -> assertEquals("In this constellation, key and value must always be equivalent",
                                           key, recursive(key).getKey()));
        assertEquals(INT_BOUND, CACHE.size());
    }

    @Test
    public final void testSingletonMultithreaded() {
        final Map<Date, AtomicInteger> setCounters = new ConcurrentHashMap<>(0);
        final Map<Integer, Date> cache = new ConcurrentHashMap<>(0);
        final Function<Integer, Date> mapper = key -> {
            final Date result = new Date(key);
            setCounters.computeIfAbsent(result, ignored -> new AtomicInteger(0)).getAndIncrement();
            return result;
        };

        final List<Thread> threads = Stream.generate(() -> new Thread(() -> {
            final Random random = new Random();
            Stream.generate(() -> random.nextInt(INT_BOUND))
                  .limit(LOOP_LIMIT / THREAD_LIMIT)
                  .forEach(key -> assertEquals("In this constellation, key and value must always be equivalent",
                                               key.longValue(), cache.computeIfAbsent(key, mapper).getTime()));
        }))
                                           .limit(THREAD_LIMIT)
                                           .collect(Collectors.toList());
        threads.forEach(Thread::start);
        threads.forEach(CacheTest::join);

        setCounters.forEach((key, value) -> assertEquals("no singleton: " + key, 1, value.get()));
        assertEquals(INT_BOUND, cache.size());
    }

    private static void join(final Thread thread) {
        try {
            thread.join();
        } catch (final InterruptedException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    @Test
    public final void test() {

    }

    private static class Recursive {

        private final int key;
        private final Lazy<List<Recursive>> parents;

        private Recursive(final int key) {
            this.key = key;
            this.parents = new Lazy<>(this::newParents);
        }

        private List<Recursive> newParents() {
            return IntStream.range(0, key)
                            .mapToObj(CacheTest::recursive)
                            .collect(Collectors.toList());
        }

        private Integer getKey() {
            return key;
        }
    }
}
