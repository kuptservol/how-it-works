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
 * Benchmark                                                          (arraySize)  Mode  Cnt     Score     Error  Units
 * SimpleParallelStreamsBench.cloneStream                                      10  avgt   50     2,182 ±   0,337  us/op
 * SimpleParallelStreamsBench.cloneStream                                   10000  avgt   50  2548,189 ±  31,155  us/op
 * SimpleParallelStreamsBench.javaParallelStream                               10  avgt   50    13,171 ±   0,942  us/op
 * SimpleParallelStreamsBench.javaParallelStream                            10000  avgt   50   753,594 ±  52,805  us/op
 * SimpleParallelStreamsBench.javaStream                                       10  avgt   50     2,569 ±   0,213  us/op
 * SimpleParallelStreamsBench.javaStream                                    10000  avgt   50  2170,807 ±  19,443  us/op
 * SimpleParallelStreamsBench.perElTransformationChainParallelStream           10  avgt   50    41,791 ±  47,259  us/op
 * SimpleParallelStreamsBench.perElTransformationChainParallelStream        10000  avgt   50  1155,246 ±   7,773  us/op
 * SimpleParallelStreamsBench.perElTransformationChainStream                   10  avgt   50     2,083 ±   0,188  us/op
 * SimpleParallelStreamsBench.perElTransformationChainStream                10000  avgt   50  2272,520 ± 179,004  us/op
 */
@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@Warmup(iterations = 5)
@Measurement(iterations = 5)
@OutputTimeUnit(MICROSECONDS)
public class SimpleParallelStreamsBench {

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
                .filter(value -> Math.atan(value) > 0)
                .map(value -> Math.exp(value))
                .collectToList());
    }

    @Benchmark
    public void perElTransformationChainStream(Blackhole bh) throws ExecutionException, InterruptedException {
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, false);
        bh.consume(stream
                .filter(value -> Math.atan(value) > 0)
                .map(value -> Math.exp(value))
                .collectToList());
    }

    @Benchmark
    public void perElTransformationChainParallelStream(Blackhole bh) throws ExecutionException, InterruptedException {
        SimpleStream<Integer> stream = PerElementTransformStageChainStream.stream(list, true);
        bh.consume(stream
                .filter(value -> Math.atan(value) > 0)
                .map(value -> Math.exp(value))
                .collectToList());
    }

    @Benchmark
    public void javaStream(Blackhole bh) throws ExecutionException, InterruptedException {
        Stream<Integer> javaStream = list.stream();
        bh.consume(javaStream
                .filter(value -> Math.atan(value) > 0)
                .map(value -> Math.exp(value))
                .collect(Collectors.toList()));
    }

    @Benchmark
    public void javaParallelStream(Blackhole bh) throws ExecutionException, InterruptedException {
        Stream<Integer> javaStream = list.parallelStream();
        bh.consume(javaStream
                .filter(value -> Math.atan(value) > 0)
                .map(value -> Math.exp(value))
                .collect(Collectors.toList()));
    }
}
