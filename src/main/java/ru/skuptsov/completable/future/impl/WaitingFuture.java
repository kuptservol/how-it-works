package ru.skuptsov.completable.future.impl;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

public abstract class WaitingFuture<V> {
    protected volatile V result;
    protected volatile Exception exception;
    protected volatile boolean finished;

    public abstract V get() throws ExecutionException;

    public abstract V get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException;

    public static <V> WaitingFuture<V> executeAsync(Supplier<V> userFunction, Supplier<WaitingFuture<V>> waitingFutureS) {
        WaitingFuture<V> waitingFuture = waitingFutureS.get();
        new Thread(new RunnableWaitingFuture<>(userFunction, waitingFuture)).start();
        return waitingFuture;
    }

    protected void postFinished() {
    }

    private static class RunnableWaitingFuture<V> implements Runnable {
        private final Supplier<V> userFunction;
        private final WaitingFuture<V> waitingFuture;

        protected RunnableWaitingFuture(Supplier<V> userFunction, WaitingFuture<V> waitingFuture) {
            this.userFunction = userFunction;
            this.waitingFuture = waitingFuture;
        }

        @Override
        public void run() {
            try {
                waitingFuture.result = userFunction.get();
            } catch (Exception e) {
                waitingFuture.exception = e;
            } finally {
                waitingFuture.finished = true;
                waitingFuture.postFinished();
            }
        }
    }
}
