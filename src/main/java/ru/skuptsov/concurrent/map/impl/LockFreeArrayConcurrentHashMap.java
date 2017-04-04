package ru.skuptsov.concurrent.map.impl;

import sun.misc.Unsafe;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Sergey Kuptsov
 * @since 03/04/2017
 */
//some words not to forget
/*
    1. we can use AtomicReferenceArray but it accepts only Object - really? - can we switch?
    2. in volatileGetNode we can now read next values safely cause there is a memory barier
    3. why we need to block in get - other solution is Harris'а для lock-free ordered list
     */
public class LockFreeArrayConcurrentHashMap<K, V> extends BaseMap<K, V> implements Map<K, V> {
    // long adder
    // add some javadocs
    // no locks
    private final LongAdder count = new LongAdder();
    private final Node<K, V>[] buckets;

    @SuppressWarnings({"rawtypes", "unchecked"})
    public LockFreeArrayConcurrentHashMap(int capacity) {
        buckets = (Node<K, V>[]) new Node[capacity];
    }

    @Override
    public int size() {
        return count.intValue();
    }

    public V get(Object key) {
        if (key == null) throw new IllegalArgumentException();
        int hash = hash(key);
        Node<K, V> node;

        // volatile read of bucket head at hash index
        if ((node = volatileGetNode(getBucketIndex(hash))) != null) {
            // check first node
            if (isKeyEquals(key, hash, node)) {
                return node.value;
            }

            // walk through the rest
            while ((node = node.next) != null) {
                if (isKeyEquals(key, hash, node))
                    return node.value;
            }
        }

        return null;
    }

    @Override
    public V put(K key, V value) {
        if (key == null || value == null) throw new IllegalArgumentException();
        int hash = hash(key);
        // no resize in this implementation - so we index will not change
        int bucketIndex = getBucketIndex(hash);

        while (true) {
            Node<K, V> node;
            // if bucket is empty try to set new head with cas
            if ((node = volatileGetNode(bucketIndex)) == null) {
                if (compareAndSwapNode(bucketIndex, null,
                        new Node<>(hash, key, value, null))) {
                    // if we succeed to set head - then break and return null

                    // is it atomic? -  haed could be deleted already here - but if deleted -
                    // there will be decrement plus increment = 0 - maybe ok?
                    count.increment();
                    break;
                }
            } else {
                // head is not null - try to find place to insert or update under lock
                synchronized (node) {
                    // check if node have not been changed since we got it
                    // otherwise let's go to another loop iteration
                    if (volatileGetNode(bucketIndex) == node) {
                        V prevValue = null;
                        Node<K, V> n = node;
                        while (true) {
                            if (isKeyEquals(key, hash, n)) {
                                prevValue = n.value;
                                n.value = value;
                                break;
                            }

                            Node<K, V> prevNode = n;
                            if ((n = n.next) == null) {
                                prevNode.next = new Node<>(hash, key, value, null);
                                count.increment();
                                break;
                            }
                        }

                        return prevValue;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public V remove(Object key) {
        if (key == null) throw new IllegalArgumentException();
        int hash = hash(key);

        return null;
    }

    private int hash(Object key) {
        return key.hashCode();
    }

    private int getBucketIndex(int hash) {
        return hash % buckets.length;
    }

    private boolean isKeyEquals(Object key, int hash, Node<K, V> node) {
        return node.hash == hash &&
                node.key == key ||
                (node.key != null && node.key.equals(key));
    }

    private static class Node<K, V> {
        final int hash;
        K key;
        // now volatile
        volatile V value;
        // now volatile
        volatile Node<K, V> next;

        Node(int hash, K key, V value, Node<K, V> next) {
            this.hash = hash;
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // completely new
    /* ---------------- Volatile bucket array access -------------- */

    @SuppressWarnings("unchecked")
    private <K, V> Node<K, V> volatileGetNode(int i) {
        return (Node<K, V>) U.getObjectVolatile(buckets, ((long) i << ASHIFT) + ABASE);
    }

    private <K, V> boolean compareAndSwapNode(int i, Node<K, V> expectedNode, Node<K, V> setNode) {
        return U.compareAndSwapObject(buckets, ((long) i << ASHIFT) + ABASE, expectedNode, setNode);
    }

    private static final sun.misc.Unsafe U;
    private static final long ABASE;
    private static final int ASHIFT;

    static {
        // get unsafe by reflection - it is illegal to use not in java lib
        try {
            Constructor<Unsafe> unsafeConstructor = Unsafe.class.getDeclaredConstructor();
            unsafeConstructor.setAccessible(true);
            U = unsafeConstructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | InvocationTargetException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        Class<?> ak = Node[].class;

        ABASE = U.arrayBaseOffset(ak);
        int scale = U.arrayIndexScale(ak);
        ASHIFT = 31 - Integer.numberOfLeadingZeros(scale);
    }
    
    /* ---------------- Volatile bucket array access -------------- */
}
