package ru.skuptsov.thread.pool.counters;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static java.lang.String.format;

/**
 * @author Sergey Kuptsov
 * @since 01/03/2017
 * <p>
 * 4:
 * -2.4709739987083262E8
 * Executed by 23 s
 * <p>
 * 8:
 * -2.4709739987083262E8
 * Executed by 17 s
 * <p>
 * 16:
 * -2.4709739987083262E8
 * Executed by 18 s
 * <p>
 * 400:
 * -2.4709739987083262E8
 * Executed by 15 s
 */
public class MultithreadClient {

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        ExecutorService threadPool = Executors.newFixedThreadPool(8);
//        ThreadPool threadPool = new ThreadPool(8);
        Counter counter = new Counter();

        long start = System.nanoTime();

        List<Future<Double>> futures = new ArrayList<>();
        for (int i = 0; i < 400; i++) {
            final int j = i;
            futures.add(
                    CompletableFuture.supplyAsync(
                            () -> counter.count(j),
                            threadPool
                    ));
        }

        double value = 0;
        for (Future<Double> future : futures) {
            value += future.get();
        }

        System.out.println(format("Executed by %d s, value : %f",
                (System.nanoTime() - start) / (1000_000_000),
                value));

        threadPool.shutdown();
    }
}
