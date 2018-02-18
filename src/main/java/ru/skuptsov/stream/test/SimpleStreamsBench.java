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
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import ru.skuptsov.stream.SimpleStream;
import ru.skuptsov.stream.impl.CloningListStream;
import ru.skuptsov.stream.impl.PerElementTransformStageChainStream;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class SimpleStreamsBench {

    private SimpleStream<Integer> cloneStream;
    private SimpleStream<Integer> perElTransformationChaonStream;
    private Stream<Integer> javaStream;
    private List<Integer> list;

    @Setup
    public void setup() {
        list = new ArrayList<>(1000);
        for (int i = 0; i < 10000; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }

        list = Collections.unmodifiableList(list);
    }

    @Benchmark
    public void testCloneStream(Blackhole bh) throws ExecutionException, InterruptedException {
        cloneStream = CloningListStream.stream(list);
        bh.consume(cloneStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collectToList());
    }

    @Benchmark
    public void perElTransformationChainStream(Blackhole bh) throws ExecutionException, InterruptedException {
        perElTransformationChaonStream = PerElementTransformStageChainStream.stream(list);
        bh.consume(perElTransformationChaonStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collectToList());
    }

    @Benchmark
    public void testJavaStream(Blackhole bh) throws ExecutionException, InterruptedException {
        javaStream = list.stream();
        bh.consume(javaStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collect(Collectors.toList()));
    }

    @Benchmark
    public void testJavaParallelStream(Blackhole bh) throws ExecutionException, InterruptedException {
        javaStream = list.parallelStream();
        bh.consume(javaStream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1)
                .filter(value -> value % 3 == 0)
                .map(value -> value * 2)
                .collect(Collectors.toList()));
    }
}
