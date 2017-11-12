package ru.skuptsov.stream.test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ThreadLocalRandom;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
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

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@State(Scope.Thread)
@Warmup(iterations = 5, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(3)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(MICROSECONDS)
public class SimpleStreamsBench {

    @Param({"cloning"})
    private String type;

    private SimpleStream<Integer> stream;

    @Setup
    public void setup() {
        List<Integer> list = new ArrayList<>(1000);
        for (int i = 0; i < 1000; i++) {
            list.add(ThreadLocalRandom.current().nextInt());
        }

        switch (type) {
            case "cloning":
                stream = CloningListStream.stream(list);
                break;
        }
    }

    @Benchmark
    public void test(Blackhole bh) throws ExecutionException, InterruptedException {
        bh.consume(stream
                .filter(value -> value % 2 == 0)
                .map(value -> value + 1));
    }
}
