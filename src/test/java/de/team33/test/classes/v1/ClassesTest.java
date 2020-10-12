package de.team33.test.classes.v1;

import de.team33.libs.classes.v1.Classes;
import org.junit.Ignore;
import org.junit.Test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

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
            Cloneable.class,
            Inner.class,
            Super.class,
            Base.class,
            ISuper1.class,
            ISuper2.class,
            ISuper3.class
    );

    @Test
    public void distance() {
        assertEquals(0, Classes.distance(Inner.class, Inner.class));
        assertEquals(1, Classes.distance(Inner.class, Super.class));
        assertEquals(2, Classes.distance(Inner.class, Base.class));
        assertEquals(3, Classes.distance(Inner.class, Object.class));
        assertEquals(2, Classes.distance(Inner.class, ISuper1.class));
        assertEquals(1, Classes.distance(Inner.class, ISuper2.class));
        assertEquals(1, Classes.distance(ISuper2.class, ISuper1.class));
        assertEquals(1, Classes.distance(Super.class, ISuper1.class));
        assertEquals(3, Classes.distance(Inner.class, ISuper3.class));

        assertEquals(2, Classes.distance(ArrayList.class, Collection.class));
        assertEquals(1, Classes.distance(ArrayList.class, AbstractList.class));
        assertEquals(3, Classes.distance(ArrayList.class, Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceInterfaceToObject() {
        fail("Should fail but was " + Classes.distance(List.class, Object.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceReverse() {
        fail("Should fail but was " + Classes.distance(Super.class, Inner.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void distanceNonRelated() {
        fail("Should fail but was " + Classes.distance(List.class, StringBuilder.class));
    }

    @Test(expected = NullPointerException.class)
    public void distanceAnyNull() {
        fail("Should fail but was " + Classes.distance(List.class, null));
    }

    @Test(expected = NullPointerException.class)
    public void distanceNullAny() {
        fail("Should fail but was " + Classes.distance(null, List.class));
    }

    @Test(expected = NullPointerException.class)
    public void distanceNullNull() {
        fail("Should fail but was " + Classes.distance(null, null));
    }

    @Test
    public void wide() {
        assertEquals(Arrays.asList("interface de.team33.test.classes.v1.ClassesTest$ISuper1",
                                   "interface de.team33.test.classes.v1.ClassesTest$ISuper2",
                                   "interface de.team33.test.classes.v1.ClassesTest$ISuper3",
                                   "class java.lang.Object",
                                   "class de.team33.test.classes.v1.ClassesTest$Base",
                                   "class de.team33.test.classes.v1.ClassesTest$Super",
                                   "class de.team33.test.classes.v1.ClassesTest$Inner"),
                     Classes.wideStreamOf(Inner.class).map(Class::toString).collect(Collectors.toList()));
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
        return superClass.equals(subClass) || Classes.streamSuperior(subClass)
                                                     .anyMatch(sub -> isAnalysedLineage(superClass, sub));
    }

    private interface ISuper1 {
    }

    private interface ISuper2 extends ISuper1 {
    }

    private interface ISuper3 {
    }

    private static class Inner extends Super implements ISuper2 {
    }

    private static class Super extends Base implements ISuper1 {
    }

    private static class Base implements ISuper3 {
    }
}
