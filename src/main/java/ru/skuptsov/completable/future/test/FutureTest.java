package ru.skuptsov.completable.future.test;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Supplier;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.skuptsov.completable.future.impl.BlockingWaitingFuture;
import ru.skuptsov.completable.future.impl.SpinLoopWaitingFuture;
import ru.skuptsov.completable.future.impl.SpinThenBlockingWaitingFuture;
import ru.skuptsov.completable.future.impl.WaitingFuture;

import static org.testng.Assert.assertTrue;

public class FutureTest {

    @DataProvider
    public Object[][] impls() {
        return new Object[][]{
                {SpinLoopWaitingFuture.class},
                {BlockingWaitingFuture.class},
                {SpinThenBlockingWaitingFuture.class}
        };
    }

    @Test(dataProvider = "impls", expectedExceptions = TimeoutException.class)
    public void testTimeout(Class<WaitingFuture> waitingFutureClass) throws TimeoutException, ExecutionException, IllegalAccessException, InstantiationException {
        Supplier<Boolean> function = () -> {
            sleep(2000);
            return true;
        };

        WaitingFuture<Boolean> future = WaitingFuture.executeAsync(function, getWaitingFutureSupplier(waitingFutureClass));
        future.get(1000, TimeUnit.MILLISECONDS);
    }

    @Test(dataProvider = "impls", expectedExceptions = ExecutionException.class)
    public void testException(Class<WaitingFuture> waitingFutureClass) throws TimeoutException, ExecutionException, IllegalAccessException, InstantiationException {
        Supplier<Boolean> function = () -> {
            sleep(1000);
            throw new RuntimeException();
        };

        WaitingFuture<Boolean> future = WaitingFuture.executeAsync(function, getWaitingFutureSupplier(waitingFutureClass));
        future.get(3000, TimeUnit.MILLISECONDS);
    }

    @Test(dataProvider = "impls")
    public void testBlockingWait(Class<WaitingFuture> waitingFutureClass) throws TimeoutException, ExecutionException, IllegalAccessException, InstantiationException {
        Supplier<Boolean> function = () -> {
            sleep(2000);
            return true;
        };

        WaitingFuture<Boolean> future = WaitingFuture.executeAsync(function, getWaitingFutureSupplier(waitingFutureClass));
        assertTrue(future.get(3000, TimeUnit.MILLISECONDS));
    }

    private Supplier<WaitingFuture<Boolean>> getWaitingFutureSupplier(Class<WaitingFuture> waitingFutureClass) {
        return () -> {
            try {
                return waitingFutureClass.newInstance();
            } catch (Exception e) {
                throw new RuntimeException();
            }
        };
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
