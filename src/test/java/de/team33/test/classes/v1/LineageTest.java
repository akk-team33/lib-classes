package de.team33.test.classes.v1;

import de.team33.libs.classes.v1.Lineage;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class LineageTest {

    @SuppressWarnings("UseOfClone")
    private static final List<Class<?>> CLASSES = asList(
            Object.class,
            Integer.class,
            BigInteger.class,
            Number.class,
            String.class,
            StringBuilder.class,
            CharSequence.class,
            Collection.class,
            List.class,
            Set.class,
            AbstractList.class,
            ArrayList.class,
            HashSet.class,
            AbstractSet.class,
            Serializable.class,
            Cloneable.class);

    @Test(timeout = 10000)
    public final void contains() {
        for (final Class<?> entry : CLASSES) {
            final Lineage lineage = Lineage.of(entry);
            assertEquals(true, lineage.contains(entry));
        }
    }

    @Test
    public final void stream() {
        assertEquals(setOf(Object.class), Lineage.of(Object.class).stream().collect(Collectors.toSet()));
    }

    @SafeVarargs
    private static <T> Set<T> setOf(final T... elements) {
        return new HashSet<>(asList(elements));
    }
}
