package de.team33.test.classes.v1;

import org.junit.Test;

import java.io.Serializable;
import java.math.BigInteger;
import java.util.AbstractCollection;
import java.util.AbstractList;
import java.util.AbstractSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.RandomAccess;
import java.util.Set;
import java.util.stream.Collectors;

import static de.team33.libs.classes.v1.Classes.Streaming.LINEAGE_HIERARCHY;
import static de.team33.libs.classes.v1.Classes.Streaming.INTERFACES;
import static de.team33.libs.classes.v1.Classes.Streaming.LINEAR_DESCENT;
import static de.team33.libs.classes.v1.Classes.Streaming.OPTIONAL;
import static de.team33.libs.classes.v1.Classes.Streaming.SUPERIOR;
import static de.team33.libs.classes.v1.Classes.Streaming.SUPER_CLASS;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

public class ClassesStreamingTest {

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
            Cloneable.class
    );
    private static final Class<?> ABSTRACT_STRING_BUILDER_CLASS = StringBuilder.class.getSuperclass();

    @Test
    public void optional() {
        CLASSES.forEach(subject -> assertEquals(singletonList(subject),
                                                OPTIONAL.apply(subject)
                                                        .collect(Collectors.toList())));
        assertEquals(emptyList(), OPTIONAL.apply(null)
                                          .collect(Collectors.toList()));
    }

    @Test
    public void superClass() {
        CLASSES.stream()
               .filter(subject -> null != subject.getSuperclass())
               .forEach(subject -> assertEquals(singletonList(subject.getSuperclass()),
                                                SUPER_CLASS.apply(subject)
                                                           .collect(Collectors.toList())));
        CLASSES.stream()
               .filter(subject -> null == subject.getSuperclass())
               .forEach(subject -> assertEquals(emptyList(),
                                                SUPER_CLASS.apply(subject)
                                                           .collect(Collectors.toList())));
    }

    @Test
    public void interfaces() {
        CLASSES.forEach(subject -> assertEquals(asList(subject.getInterfaces()),
                                                INTERFACES.apply(subject)
                                                          .collect(Collectors.toList())));
    }

    @Test
    public void superior() {
        CLASSES.forEach(subject -> {
            final List<Class<?>> expected = new ArrayList<>(asList(subject.getInterfaces()));
            Optional.ofNullable(subject.getSuperclass())
                    .ifPresent(expected::add);
            assertEquals(expected,
                         SUPERIOR.apply(subject)
                                 .collect(Collectors.toList()));
        });
    }

    @Test
    public void linearDescent() {
        linearDescent(Object.class, Object.class);
        linearDescent(Integer.class, Object.class, Number.class, Integer.class);
        linearDescent(BigInteger.class, Object.class, Number.class, BigInteger.class);
        linearDescent(Number.class, Object.class, Number.class);
        linearDescent(String.class, Object.class, String.class);
        linearDescent(StringBuilder.class, Object.class, ABSTRACT_STRING_BUILDER_CLASS, StringBuilder.class);
        linearDescent(CharSequence.class, CharSequence.class);
        linearDescent(Collection.class, Collection.class);
        linearDescent(List.class, List.class);
        linearDescent(Set.class, Set.class);
        linearDescent(AbstractList.class, Object.class, AbstractCollection.class, AbstractList.class);
        linearDescent(ArrayList.class, Object.class, AbstractCollection.class, AbstractList.class, ArrayList.class);
        linearDescent(HashSet.class, Object.class, AbstractCollection.class, AbstractSet.class, HashSet.class);
        linearDescent(AbstractSet.class, Object.class, AbstractCollection.class, AbstractSet.class);
        linearDescent(Serializable.class, Serializable.class);
        linearDescent(Cloneable.class, Cloneable.class);
    }

    private void linearDescent(final Class<?> subject, final Class<?> ... expected) {
        assertEquals(asList(expected), LINEAR_DESCENT.apply(subject).collect(Collectors.toList()));
    }

    @Test
    public void lineageHierarchy() {
        lineageHierarchy(Object.class, Object.class);
        lineageHierarchy(Integer.class, Comparable.class, Serializable.class, Object.class, Number.class,
                         Integer.class);
        lineageHierarchy(BigInteger.class, Comparable.class, Serializable.class, Object.class, Number.class,
                         BigInteger.class);
        lineageHierarchy(Number.class, Serializable.class, Object.class, Number.class);
        lineageHierarchy(String.class, Serializable.class, Comparable.class, CharSequence.class, Object.class,
                         String.class);
        lineageHierarchy(StringBuilder.class, Serializable.class, CharSequence.class, Appendable.class, Object.class,
                         ABSTRACT_STRING_BUILDER_CLASS, StringBuilder.class);
        lineageHierarchy(CharSequence.class, CharSequence.class);
        lineageHierarchy(Collection.class, Iterable.class, Collection.class);
        lineageHierarchy(List.class, Iterable.class, Collection.class, List.class);
        lineageHierarchy(Set.class, Iterable.class, Collection.class, Set.class);
        lineageHierarchy(AbstractList.class, Iterable.class, Collection.class, List.class, Object.class,
                         AbstractCollection.class, AbstractList.class);
        lineageHierarchy(ArrayList.class, Iterable.class, Collection.class, List.class, RandomAccess.class,
                         Cloneable.class, Serializable.class, Object.class, AbstractCollection.class,
                         AbstractList.class, ArrayList.class);
        lineageHierarchy(HashSet.class, Iterable.class, Collection.class, Set.class, Cloneable.class,
                         Serializable.class, Object.class, AbstractCollection.class, AbstractSet.class, HashSet.class);
        lineageHierarchy(AbstractSet.class, Iterable.class, Collection.class, Set.class, Object.class,
                         AbstractCollection.class, AbstractSet.class);
        lineageHierarchy(Serializable.class, Serializable.class);
        lineageHierarchy(Cloneable.class, Cloneable.class);
    }

    private void lineageHierarchy(final Class<?> subject, final Class<?> ... expected) {
        assertEquals(asList(expected), LINEAGE_HIERARCHY.apply(subject).collect(Collectors.toList()));
    }
}
