package ru.skuptsov.concurrent.map;

/**
 * @author Sergey Kuptsov
 * @since 26/03/2017
 */
public interface IMap<K, V> {

    V put(K key, V value);

    V get(Object key);

    int size();
}
