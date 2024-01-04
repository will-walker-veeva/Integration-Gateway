package com.veeva.vault.custom.app.model.query;

import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

/**
 * Result holder class for a Query Response
 * @param <T>
 */
public class QueryResult <T> implements Iterable<T>{
    private List<T> results;

    public QueryResult(List<T> results) {
        this.results = results;
    }

    public List<T> getResults() {
        return results;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return results.iterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
       results.forEach(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return results.spliterator();
    }
}
