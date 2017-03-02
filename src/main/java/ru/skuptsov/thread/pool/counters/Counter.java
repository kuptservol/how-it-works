package ru.skuptsov.thread.pool.counters;

/**
 * @author Sergey Kuptsov
 * @since 01/03/2017
 */
public class Counter {

    public Double count(double a) {
        for (int i = 0; i < 1000000; i++) {
            a = a + Math.tan(a);
        }

        return a;
    }
}
