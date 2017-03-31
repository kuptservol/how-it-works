package ru.skuptsov.concurrent.map.impl;

import ru.skuptsov.concurrent.map.IMap;

import java.util.Map;

/**
 * @author Sergey Kuptsov
 * @since 31/03/2017
 */
public class SynchronizedHashMap<K, V> extends BaseMap<K, V> implements Map<K, V>, IMap<K, V> {

    private final Map<K, V> provider;
    private final Object monitor = new Object();

    public SynchronizedHashMap(Map<K, V> provider) {
        this.provider = provider;
    }

    @Override
    public V put(K key, V value) {
        synchronized (monitor) {
            return provider.put(key, value);
        }
    }

    @Override
    public V get(Object key) {
        synchronized (monitor) {
            return provider.get(key);
        }
    }

    @Override
    public int size() {
        synchronized (monitor) {
            return provider.size();
        }
    }
}
