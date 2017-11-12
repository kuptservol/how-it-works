package ru.skuptsov.completable.future.medium;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public interface WaitingFuture<V> {

    V get() throws ExecutionException;

    V get(long timeout, TimeUnit unit) throws TimeoutException, ExecutionException;
}
