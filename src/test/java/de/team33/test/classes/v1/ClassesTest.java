package de.team33.test.classes.v1;

import de.team33.libs.classes.v1.Classes;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractSequentialList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class ClassesTest {

    private static final List<Class<?>> CLASSES = Arrays.asList(
            void.class,
            int.class,
            Void.class,
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
            Cloneable.class
    );

    @Test
    public void distance() {
        CLASSES.forEach(anyClass -> {
            assertEquals(0, Classes.distance(anyClass, anyClass));
            Optional.ofNullable(anyClass.getSuperclass())
                    .ifPresent(superClass -> assertEquals(1, Classes.distance(superClass, anyClass)));
            Stream.of(anyClass.getInterfaces())
                  .forEach(superClass -> assertEquals(1, Classes.distance(superClass, anyClass)));
        });

        assertEquals(2, Classes.distance(Object.class, Integer.class));
        assertEquals(2, Classes.distance(Object.class, BigInteger.class));
        assertEquals(2, Classes.distance(Object.class, StringBuilder.class));
        assertEquals(2, Classes.distance(Object.class, AbstractList.class));
        assertEquals(2, Classes.distance(Object.class, AbstractSet.class));
        assertEquals(2, Classes.distance(Collection.class, AbstractList.class));
        assertEquals(2, Classes.distance(Collection.class, ArrayList.class));
        assertEquals(2, Classes.distance(Collection.class, HashSet.class));
        assertEquals(2, Classes.distance(Collection.class, AbstractSet.class));
        assertEquals(2, Classes.distance(Serializable.class, Integer.class));
        assertEquals(2, Classes.distance(Serializable.class, BigInteger.class));
        assertEquals(2, Classes.distance(Collection.class, LinkedList.class));

        assertEquals(3, Classes.distance(Object.class, ConcurrentSkipListSet.class));
        assertEquals(3, Classes.distance(Collection.class, AbstractSequentialList.class));
        assertEquals(3, Classes.distance(Object.class, AbstractSequentialList.class));
        assertEquals(3, Classes.distance(Object.class, ArrayList.class));
        assertEquals(3, Classes.distance(Object.class, HashSet.class));

        assertEquals(4, Classes.distance(Object.class, LinkedList.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceInterfaceToObject() {
        fail("Should fail but was " + Classes.distance(Object.class, List.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceReverse() {
        fail("Should fail but was " + Classes.distance(ArrayList.class, Collection.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceNonRelated() {
        fail("Should fail but was " + Classes.distance(StringBuilder.class, List.class));
    }

    @Test(expected = NullPointerException.class)
    public void distanceAnyNull() {
        fail("Should fail but was " + Classes.distance(null, List.class));
    }

    @Test(expected = NullPointerException.class)
    public void distanceNullAny() {
        fail("Should fail but was " + Classes.distance(List.class, null));
    }

    @Test(expected = NullPointerException.class)
    public void distanceNullNull() {
        fail("Should fail but was " + Classes.distance(null, null));
    }

    @Test
    public void superiorNull() {
        assertEquals(0, Classes.superior(null).count());
    }

    @Test
    public void lineageClassesNull() {
        assertEquals(0, Classes.lineageClasses(null).count());
    }

    @Test
    public void lineageHierarchyNull() {
        assertEquals(0, Classes.lineageHierarchy(null).count());
    }

    @Test
    public void isLineage() {
        for (Class<?> superClass : CLASSES) {
            isLineage(superClass);
        }
    }

    private void isLineage(final Class<?> superClass) {
        for (Class<?> subClass : CLASSES) {
            isLineage(superClass, subClass);
        }
    }

    private void isLineage(final Class<?> superClass, final Class<?> subClass) {
        final boolean assignable = superClass.isAssignableFrom(subClass);
        final boolean lineage = Classes.isLineage(superClass, subClass);
        final String message = superClass + " > " + subClass;

        // assert Classes.isLineage(...) in relation to Class.isAssignable(...) ...
        if (Object.class.equals(superClass) && subClass.isInterface()) {
            assertTrue(message, assignable);
            assertFalse(message, lineage);
        } else {
            assertEquals(message, assignable, lineage);
        }

        // assert Classes.isLineage(...) by complete analysis ...
        assertEquals(message, isAnalysedLineage(superClass, subClass), lineage);
    }

    private boolean isAnalysedLineage(final Class<?> superClass, final Class<?> subClass) {
        return superClass.equals(subClass) || Classes.superior(subClass)
                                                     .anyMatch(sub -> isAnalysedLineage(superClass, sub));
    }
}
