package ru.skuptsov.stream;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import ru.skuptsov.stream.impl.CloningListStream;

public interface SimpleStream<T> {

    SimpleStream<T> filter(Predicate<? super T> predicate);

    <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper);

    List<T> collectToList();
}
