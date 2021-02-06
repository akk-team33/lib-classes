package de.team33.libs.classes.v1;

import de.team33.libs.lazy.v1.Lazy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Collections.unmodifiableList;

/**
 * Abstracts the lineage hierarchy of a particular class
 */
public final class Lineage {

    private static final Map<Class<?>, Lazy<Lineage>> CACHE = new ConcurrentHashMap<>(0);
    private static final Function<Class<?>, Lazy<Lineage>> NEW_LAZY_LINEAGE = key ->
            new Lazy<>(() -> new Lineage(key));

    private final Class<?> subject;
    private final List<Lineage> superior;
    private final transient Lazy<List<Object>> listView = new Lazy<>(this::newListView);

    private Lineage(final Class<?> subject) {
        this.subject = subject;
        this.superior = newSuperior();
    }

    public static Lineage of(final Class<?> subject) {
        return CACHE.computeIfAbsent(subject, NEW_LAZY_LINEAGE).get();
    }

    private List<Lineage> newSuperior() {
        return unmodifiableList(new ArrayList<>(Basics.streamOfSuperior(subject)
                                                      .map(Lineage::of)
                                                      .collect(LinkedList::new, List::add, List::addAll)));
    }

    private List<Object> newListView() {
        return Arrays.asList(subject, superior);
    }

    public boolean contains(final Class<?> other) {
        return stream().anyMatch(other::equals);
    }

    public final Stream<Class<?>> stream() {
        return Stream.concat(superior.stream()
                                     .flatMap(Lineage::stream),
                             Stream.of(subject));
    }

    @Override
    public int hashCode() {
        return listView.get().hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return (this == obj) || ((obj instanceof Lineage) && listView.get().equals(((Lineage) obj).listView.get()));
    }

    @Override
    public final String toString() {
        return listView.get().toString();
    }
}
