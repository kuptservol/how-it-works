package ru.skuptsov.concurrent.map.test;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.skuptsov.concurrent.map.impl.SynchronizedHashMap;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * @author Sergey Kuptsov
 * @since 30/03/2017
 */
@State(Scope.Thread)
@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class ConcurrentMapBenchmark {
    private Map<Integer, Integer> map;

    @Param({"concurrenthashmap", "hashtable", "synchronizedhashmap"})
    private String type;

    @Param({"1", "10"})
    private Integer writersNum;

    @Param({"1", "10"})
    private Integer readersNum;

    private final static int NUM = 1000;

    @Setup
    public void setup() {
        switch (type) {
            case "hashtable":
                map = new Hashtable<>();
                break;
            case "concurrenthashmap":
                map = new ConcurrentHashMap<>();
                break;
            case "synchronizedhashmap":
                map = new SynchronizedHashMap<>(new HashMap<>());
                break;
        }
    }

    @Benchmark
    public void test(Blackhole bh) throws ExecutionException, InterruptedException {

        List<CompletableFuture> futures = new ArrayList<>();

        for (int i = 0; i < writersNum; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < NUM; j++) {
                    map.put(j, j);
                }
            }));
        }

        for (int i = 0; i < readersNum; i++) {
            futures.add(CompletableFuture.runAsync(() -> {
                for (int j = 0; j < NUM; j++) {
                    bh.consume(map.get(j));
                }
            }));
        }

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[1])).get();
    }
}

