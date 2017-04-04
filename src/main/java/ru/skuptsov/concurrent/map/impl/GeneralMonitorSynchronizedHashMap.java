package ru.skuptsov.concurrent.map.impl;

import java.util.Map;

/**
 * @author Sergey Kuptsov
 * @since 31/03/2017
 */
public class GeneralMonitorSynchronizedHashMap<K, V> extends BaseMap<K, V> implements Map<K, V> {

    private final Map<K, V> provider;
    private final Object monitor;

    public GeneralMonitorSynchronizedHashMap(Map<K, V> provider) {
        this.provider = provider;
        monitor = this;
    }

    @Override
    public V put(K key, V value) {
        synchronized (monitor) {
            return provider.put(key, value);
        }
    }

    @Override
    public V remove(Object key) {
        synchronized (monitor) {
            return provider.remove(key);
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
