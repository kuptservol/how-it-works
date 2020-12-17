package ru.skuptsov.stream.test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import ru.skuptsov.stream.SimpleStream;
import ru.skuptsov.stream.impl.CloningListStream;
import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

/**
 * Benchmark                                                  (arraySize)  Mode  Cnt    Score    Error  Units
 * SimpleStreamsBench.cloneStream                                      10  avgt   50    0,371 ±  0,048  us/op
 * SimpleStreamsBench.cloneStream                                   10000  avgt   50  361,038 ± 24,312  us/op
 * SimpleStreamsBench.javaParallelStream                               10  avgt   50    8,326 ±  0,613  us/op
 * SimpleStreamsBench.javaParallelStream                            10000  avgt   50  146,177 ±  7,580  us/op
 * SimpleStreamsBench.javaStream                                       10  avgt   50    0,346 ±  0,057  us/op
 * SimpleStreamsBench.javaStream                                    10000  avgt   50  253,245 ± 25,030  us/op
 * SimpleStreamsBench.perElTransformationChainParallelStream           10  avgt   50   21,088 ± 11,783  us/op
 * SimpleStreamsBench.perElTransformationChainParallelStream        10000  avgt   50  426,810 ± 65,874  us/op
 * SimpleStreamsBench.perElTransformationChainStream                   10  avgt   50    0,201 ±  0,013  us/op
 * SimpleStreamsBench.perElTransformationChainStream                10000  avgt   50  236,274 ±  2,898  us/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@OutputTimeUnit(MICROSECONDS)
public class SimpleStreamsBench {

    @Param({"10", "10000"})
    private Integer arraySize;

    private List<Integer> list;

    @Setup
    public void setup() {
        list = new ArrayList<>(arraySize);
        for (int i = 0; i < arraySize; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }

        list = Collections.unmodifiableList(list);
    }

    @Benchmark
    public void cloneStream(Blackhole bh) throws ExecutionException, InterruptedException {
        SimpleStream<Integer> cloneStream = CloningListStream.stream(list);
        bh.consume(cloneStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collectToList());
    }

    @Benchmark
    public void perElTransformationChainStream(Blackhole bh) throws ExecutionException, InterruptedException {
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, false);
        bh.consume(stream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collectToList());
    }

    @Benchmark
    public void perElTransformationChainParallelStream(Blackhole bh) throws ExecutionException, InterruptedException {
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, true);
        bh.consume(stream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collectToList());
    }

    @Benchmark
    public void javaStream(Blackhole bh) throws ExecutionException, InterruptedException {
        Stream<Integer> javaStream = list.stream();
        bh.consume(javaStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collect(Collectors.toList()));
    }

    @Benchmark
    public void javaParallelStream(Blackhole bh) throws ExecutionException, InterruptedException {
        Stream<Integer> javaStream = list.parallelStream();
        bh.consume(javaStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collect(Collectors.toList()));
    }
}
