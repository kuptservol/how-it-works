package ru.skuptsov.completable.future.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class SpinLoopWaitingFuture<V> extends WaitingFuture<V> {

    @Override
    public V get() throws ExecutionException {
        while (!finished) {
            //spin
        }

        if (exception != null) {
            throw new ExecutionException(exception);
        }

        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws ExecutionException, TimeoutException {
        long started = System.nanoTime();
        long allowedExecutionNanos = unit.toNanos(timeout);
        while (!finished) {
            if (System.nanoTime() - started > allowedExecutionNanos) {
                throw new TimeoutException();
            }
        }

        if (exception != null) {
            throw new ExecutionException(exception);
        }

        return result;
    }
}
