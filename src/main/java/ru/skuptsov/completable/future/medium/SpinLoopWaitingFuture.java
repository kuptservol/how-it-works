package ru.skuptsov.completable.future.medium;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public class SpinLoopWaitingFuture<V> implements WaitingFuture<V> {
    // result of user function
    private volatile V result;
    // exception if one occured
    private volatile Throwable throwable;
    // marker that indicates that user function finished
    private volatile boolean finished;

    @Override
    public V get() throws ExecutionException {
        while (!finished) {
            //spin
        }

        if (throwable != null) {
            throw new ExecutionException(throwable);
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

        if (throwable != null) {
            throw new ExecutionException(throwable);
        }

        return result;
    }

    public static <V> WaitingFuture<V> executeAsync(Supplier<V> userFunction) {
        SpinLoopWaitingFuture<V> waitingFuture = new SpinLoopWaitingFuture<>();
        new Thread(new RunnableWaitingFuture<>(userFunction, waitingFuture)).start();
        return waitingFuture;
    }

    private static class RunnableWaitingFuture<V> implements Runnable {
        private final Supplier<V> userFunction;
        private final SpinLoopWaitingFuture<V> waitingFuture;

        public RunnableWaitingFuture(Supplier<V> userFunction, SpinLoopWaitingFuture<V> waitingFuture) {
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
            }
        }
    }
}
