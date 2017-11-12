package ru.skuptsov.completable.future.medium;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Supplier;

public class BlockingWaitingFuture<V> implements WaitingFuture<V> {
    private volatile V result;
    private volatile Throwable throwable;
    private volatile boolean finished;
    private volatile Thread thread;

    @Override
    public V get() throws ExecutionException {
        this.thread = Thread.currentThread();
        LockSupport.park(this);

        if (throwable != null) {
            throw new ExecutionException(throwable);
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

        if (throwable != null) {
            throw new ExecutionException(throwable);
        }

        return result;
    }

    public static <V> WaitingFuture<V> executeAsync(Supplier<V> userFunction) {
        BlockingWaitingFuture<V> waitingFuture = new BlockingWaitingFuture<>();
        new Thread(new RunnableWaitingFuture<>(userFunction, waitingFuture)).start();
        return waitingFuture;
    }

    private static class RunnableWaitingFuture<V> implements Runnable {
        private final Supplier<V> userFunction;
        private final BlockingWaitingFuture<V> waitingFuture;

        public RunnableWaitingFuture(Supplier<V> userFunction, BlockingWaitingFuture<V> waitingFuture) {
            this.userFunction = userFunction;
            this.waitingFuture = waitingFuture;
        }

        @Override
        public void run() {
            try {
                waitingFuture.result = userFunction.get();
            } catch (Throwable throwable) {
                waitingFuture.throwable = throwable;
            } finally {
                waitingFuture.finished = true;
                LockSupport.unpark(waitingFuture.thread);
            }
        }
    }
}
