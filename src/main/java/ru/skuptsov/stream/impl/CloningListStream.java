package ru.skuptsov.stream.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import ru.skuptsov.stream.SimpleStream;

public class CloningListStream<T> implements SimpleStream<T> {

    private final List<T> list;

    public static <T> CloningListStream<T> stream(List<T> list) {
        return new CloningListStream<>(list);
    }

    private CloningListStream(List<T> list) {
        this.list = list;
    }

    @Override
    public SimpleStream<T> filter(Predicate<? super T> predicate) {
        List<T> newList = new ArrayList<>();
        for (T el : list) {
            if (predicate.test(el)) {
                newList.add(el);
            }
        }
        return stream(newList);
    }

    @Override
    public <R> SimpleStream<R> map(Function<? super T, ? extends R> mapper) {
        List<R> newList = new ArrayList<>();
        for (T el : list) {
            newList.add(mapper.apply(el));
        }
        return stream(newList);
    }

    @Override
    public List<T> collectToList() {
        return list;
    }
}
