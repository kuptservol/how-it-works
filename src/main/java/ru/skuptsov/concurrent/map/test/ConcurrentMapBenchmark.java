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
/*

Benchmark                    (readersNum)               (type)  (writersNum)  Mode  Cnt     Score     Error  Units
ConcurrentMapBenchmark.test             1    concurrenthashmap             1  avgt   15    65,157 ±   3,636  us/op
ConcurrentMapBenchmark.test             1    concurrenthashmap            10  avgt   15   302,023 ±  11,893  us/op
ConcurrentMapBenchmark.test             1            hashtable             1  avgt   15   156,149 ±   8,133  us/op
ConcurrentMapBenchmark.test             1            hashtable            10  avgt   15   736,649 ±  31,852  us/op
ConcurrentMapBenchmark.test             1  synchronizedhashmap             1  avgt   15   178,888 ±  11,110  us/op
ConcurrentMapBenchmark.test             1  synchronizedhashmap            10  avgt   15  1019,344 ±  67,259  us/op
ConcurrentMapBenchmark.test            10    concurrenthashmap             1  avgt   15   235,409 ±   6,956  us/op
ConcurrentMapBenchmark.test            10    concurrenthashmap            10  avgt   15   395,036 ±  23,586  us/op
ConcurrentMapBenchmark.test            10            hashtable             1  avgt   15   873,091 ±  65,774  us/op
ConcurrentMapBenchmark.test            10            hashtable            10  avgt   15  1564,516 ± 126,142  us/op
ConcurrentMapBenchmark.test            10  synchronizedhashmap             1  avgt   15   983,234 ± 109,709  us/op
ConcurrentMapBenchmark.test            10  synchronizedhashmap            10  avgt   15  1703,271 ± 162,345  us/op

 */
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

