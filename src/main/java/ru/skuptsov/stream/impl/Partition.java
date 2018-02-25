package ru.skuptsov.stream.impl;

import java.math.BigDecimal;
import java.util.AbstractList;
import java.util.List;

public class Partition<T> extends AbstractList<List<T>> {
    final List<T> list;
    final int size;

    Partition(List<T> list, int size) {
        this.list = list;
        this.size = size;
    }

    @Override
    public List<T> get(int index) {
        int start = index * size;
        int end = Math.min(start + size, list.size());
        return list.subList(start, end);
    }

    @Override
    public int size() {
        return BigDecimal.valueOf(list.size())
                .divide(BigDecimal.valueOf(size), BigDecimal.ROUND_CEILING)
                .intValue();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }
}
