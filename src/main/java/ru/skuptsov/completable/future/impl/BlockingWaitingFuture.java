package ru.skuptsov.completable.future.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;

public class BlockingWaitingFuture<V> extends WaitingFuture<V> {
    private volatile Thread thread;

    @Override
    public V get() throws ExecutionException {
        this.thread = Thread.currentThread();
        LockSupport.park(this);

        if (exception != null) {
            throw new ExecutionException(exception);
        }

        return result;
    }

    @Override
    public V get(long timeout, TimeUnit unit) throws ExecutionException, TimeoutException {
        this.thread = Thread.currentThread();
        long allowedExecutionNanos = unit.toNanos(timeout);
        LockSupport.parkNanos(allowedExecutionNanos);

        if (!finished) {
            throw new TimeoutException();
        }

        if (exception != null) {
            throw new ExecutionException(exception);
        }

        return result;
    }

    protected void postFinished() {
        LockSupport.unpark(thread);
    }
}
