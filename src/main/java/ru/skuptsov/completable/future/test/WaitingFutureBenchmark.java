package ru.skuptsov.completable.future.test;

import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

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
import org.openjdk.jmh.annotations.Threads;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import ru.skuptsov.completable.future.impl.BlockingWaitingFuture;
import ru.skuptsov.completable.future.impl.SpinLoopWaitingFuture;
import ru.skuptsov.completable.future.impl.SpinThenBlockingWaitingFuture;
import ru.skuptsov.completable.future.impl.WaitingFuture;

import static java.util.concurrent.TimeUnit.MICROSECONDS;

@State(Scope.Thread)
@Warmup(iterations = 1, time = 2)
@Measurement(iterations = 1, time = 2)
@Fork(1)
@BenchmarkMode(Mode.AverageTime)
@Threads(100)
@OutputTimeUnit(MICROSECONDS)
public class WaitingFutureBenchmark {

    Supplier<WaitingFuture<String>> waitingFutureImplClassS;

    @Param({"spinloop", "blocking", "spinthenloop"})
    private String impl;

    @Param({"1", "10", "1000"})
    private Integer functionTimeoutMs;

    @Setup
    public void setup() {

        switch (impl) {
            case "spinloop":
                waitingFutureImplClassS = () -> new SpinLoopWaitingFuture<>();
                break;
            case "blocking":
                waitingFutureImplClassS = () -> new BlockingWaitingFuture<>();
                break;
            case "spinthenloop":
                waitingFutureImplClassS = () -> new SpinThenBlockingWaitingFuture();
                break;
        }
    }

    @Benchmark
    public void test(Blackhole bh) throws ExecutionException, InterruptedException, IllegalAccessException, InstantiationException {
        WaitingFuture<String> future = WaitingFuture.executeAsync(function, waitingFutureImplClassS);
        bh.consume(future.get());
    }

    Supplier<String> function = () -> {
        sleep(functionTimeoutMs);
        return "OK";
    };

    private void sleep(int time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
