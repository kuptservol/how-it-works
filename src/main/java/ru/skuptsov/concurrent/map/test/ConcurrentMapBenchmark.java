package ru.skuptsov.concurrent.map.test;

import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import ru.skuptsov.concurrent.map.impl.GeneralMonitorSynchronizedHashMap;
import ru.skuptsov.concurrent.map.impl.LockFreeArrayConcurrentHashMap;
import ru.skuptsov.concurrent.map.impl.LockStripingArrayConcurrentHashMap;

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
Benchmark                    (initCapDelim)  (readersNum)                          (type)  (writersNum)  Mode  Cnt     Score     Error  Units
ConcurrentMapBenchmark.test               1             1                       hashtable             1  avgt   15   136,207 ±   4,776  us/op
ConcurrentMapBenchmark.test               1             1                       hashtable            10  avgt   15   652,474 ±  23,728  us/op
ConcurrentMapBenchmark.test               1             1   generalmonitorsynchronizedmap             1  avgt   15   179,849 ±  14,888  us/op
ConcurrentMapBenchmark.test               1             1   generalmonitorsynchronizedmap            10  avgt   15   871,531 ±  22,616  us/op
ConcurrentMapBenchmark.test               1             1          lockarrayconcurrentmap             1  avgt   15    83,811 ±   2,985  us/op
ConcurrentMapBenchmark.test               1             1          lockarrayconcurrentmap            10  avgt   15   236,211 ±   1,980  us/op
ConcurrentMapBenchmark.test               1             1  lockfreearrayconcurrenthashmap             1  avgt   15    50,247 ±   0,610  us/op
ConcurrentMapBenchmark.test               1             1  lockfreearrayconcurrenthashmap            10  avgt   15   219,513 ±   4,441  us/op
ConcurrentMapBenchmark.test               1             1               concurrenthashmap             1  avgt   15    60,179 ±   0,587  us/op
ConcurrentMapBenchmark.test               1             1               concurrenthashmap            10  avgt   15   263,760 ±   2,912  us/op
ConcurrentMapBenchmark.test               1            10                       hashtable             1  avgt   15   734,999 ±  67,916  us/op
ConcurrentMapBenchmark.test               1            10                       hashtable            10  avgt   15  1316,193 ± 114,037  us/op
ConcurrentMapBenchmark.test               1            10   generalmonitorsynchronizedmap             1  avgt   15   835,131 ± 131,299  us/op
ConcurrentMapBenchmark.test               1            10   generalmonitorsynchronizedmap            10  avgt   15  1503,386 ±  48,276  us/op
ConcurrentMapBenchmark.test               1            10          lockarrayconcurrentmap             1  avgt   15   293,081 ±   8,467  us/op
ConcurrentMapBenchmark.test               1            10          lockarrayconcurrentmap            10  avgt   15   507,097 ±  68,239  us/op
ConcurrentMapBenchmark.test               1            10  lockfreearrayconcurrenthashmap             1  avgt   15   221,541 ±  14,170  us/op
ConcurrentMapBenchmark.test               1            10  lockfreearrayconcurrenthashmap            10  avgt   15   345,817 ±  10,202  us/op
ConcurrentMapBenchmark.test               1            10               concurrenthashmap             1  avgt   15   205,495 ±   5,394  us/op
ConcurrentMapBenchmark.test               1            10               concurrenthashmap            10  avgt   15   404,479 ±  51,787  us/op
ConcurrentMapBenchmark.test               6             1                       hashtable             1  avgt   15   153,224 ±  10,370  us/op
ConcurrentMapBenchmark.test               6             1                       hashtable            10  avgt   15   722,087 ±  61,589  us/op
ConcurrentMapBenchmark.test               6             1   generalmonitorsynchronizedmap             1  avgt   15   174,878 ±  14,855  us/op
ConcurrentMapBenchmark.test               6             1   generalmonitorsynchronizedmap            10  avgt   15   929,469 ±  53,313  us/op
ConcurrentMapBenchmark.test               6             1          lockarrayconcurrentmap             1  avgt   15    81,974 ±   2,850  us/op
ConcurrentMapBenchmark.test               6             1          lockarrayconcurrentmap            10  avgt   15   275,773 ±  24,633  us/op
ConcurrentMapBenchmark.test               6             1  lockfreearrayconcurrenthashmap             1  avgt   15    66,928 ±   2,091  us/op
ConcurrentMapBenchmark.test               6             1  lockfreearrayconcurrenthashmap            10  avgt   15   274,349 ±  22,351  us/op
ConcurrentMapBenchmark.test               6             1               concurrenthashmap             1  avgt   15    61,267 ±   1,592  us/op
ConcurrentMapBenchmark.test               6             1               concurrenthashmap            10  avgt   15   272,774 ±  21,149  us/op
ConcurrentMapBenchmark.test               6            10                       hashtable             1  avgt   15   731,852 ±  58,202  us/op
ConcurrentMapBenchmark.test               6            10                       hashtable            10  avgt   15  1294,142 ± 107,070  us/op
ConcurrentMapBenchmark.test               6            10   generalmonitorsynchronizedmap             1  avgt   15   813,740 ± 100,732  us/op
ConcurrentMapBenchmark.test               6            10   generalmonitorsynchronizedmap            10  avgt   15  1515,476 ±  57,179  us/op
ConcurrentMapBenchmark.test               6            10          lockarrayconcurrentmap             1  avgt   15   331,127 ±  42,718  us/op
ConcurrentMapBenchmark.test               6            10          lockarrayconcurrentmap            10  avgt   15   507,965 ±  36,554  us/op
ConcurrentMapBenchmark.test               6            10  lockfreearrayconcurrenthashmap             1  avgt   15   225,563 ±   2,636  us/op
ConcurrentMapBenchmark.test               6            10  lockfreearrayconcurrenthashmap            10  avgt   15   398,215 ±   5,278  us/op
ConcurrentMapBenchmark.test               6            10               concurrenthashmap             1  avgt   15   220,024 ±  13,572  us/op
ConcurrentMapBenchmark.test               6            10               concurrenthashmap            10  avgt   15   383,444 ±  18,762  us/op
 */
public class ConcurrentMapBenchmark {
    private Map<Integer, Integer> map;

    @Param({"hashtable", "generalmonitorsynchronizedmap", "lockarrayconcurrentmap", "lockfreearrayconcurrenthashmap", "concurrenthashmap"})
    private String type;

    @Param({"1", "10"})
    private Integer writersNum;

    @Param({"1", "10"})
    private Integer readersNum;

    private final static int NUM = 1000;

    @Param({"1", "6"})
    private static int initCapDelim;

    @Setup
    public void setup() {
        int initCap = NUM / initCapDelim;

        switch (type) {
            case "hashtable":
                map = new Hashtable<>(initCap);
                break;
            case "concurrenthashmap":
                map = new ConcurrentHashMap<>(initCap);
                break;
            case "generalmonitorsynchronizedmap":
                map = new GeneralMonitorSynchronizedHashMap<>(new HashMap<>(initCap));
                break;
            case "lockarrayconcurrentmap":
                map = new LockStripingArrayConcurrentHashMap<>(initCap);
                break;
            case "lockfreearrayconcurrenthashmap":
                map = new LockFreeArrayConcurrentHashMap<>(initCap);
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

