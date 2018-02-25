package ru.skuptsov.stream.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ru.skuptsov.stream.SimpleStream;

public class PerElementTransformStageChainStream {

    static class StreamStage<IN, OUT> implements SimpleStream<OUT> {
        final List<?> list;
        final StreamStage prevStage;
        final Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer;
        final boolean parallel;

        StreamStage(List<?> list, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer, boolean parallel) {
            this.list = list;
            this.parallel = parallel;
            this.prevStage = null;
            this.consumerPipelineTransformer = consumerPipelineTransformer;
        }

        StreamStage(List<?> list, StreamStage<?, ?> upStream, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineTransformer, boolean parallel) {
            this.list = list;
            this.prevStage = upStream;
            this.consumerPipelineTransformer = consumerPipelineTransformer;
            this.parallel = parallel;
        }

        abstract static class TransformChain<T, OUT> implements Consumer<T> {
            protected final Consumer<? super OUT> downstream;

            TransformChain(Consumer<? super OUT> downstream) {
                this.downstream = downstream;
            }
        }

        @Override
        public SimpleStream<OUT> filter(Predicate<? super OUT> predicate) {
            return new StreamStage<OUT, OUT>(
                    list,
                    this,
                    new Function<Consumer<OUT>, Consumer<OUT>>() {
                        @Override
                        public Consumer<OUT> apply(Consumer<OUT> outConsumer) {
                            return new TransformChain<OUT, OUT>(outConsumer) {
                                @Override
                                public void accept(OUT out) {
                                    if (predicate.test(out)) {
                                        downstream.accept(out);
                                    }
                                }
                            };
                        }
                    },
                    parallel
            );
        }

        @Override
        public <R> SimpleStream<R> map(Function<? super OUT, ? extends R> mapper) {
            return new StreamStage<OUT, R>(
                    list,
                    this,
                    new Function<Consumer<R>, Consumer<OUT>>() {
                        @Override
                        public Consumer<OUT> apply(Consumer<R> outConsumer) {
                            return new TransformChain<OUT, R>(outConsumer) {
                                @Override
                                public void accept(OUT out) {
                                    downstream.accept(mapper.apply(out));
                                }
                            };
                        }
                    },
                    parallel
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<OUT> collectToList() {
            if (parallel) {
                return processParallel();
            } else {
                return processSerial();
            }
        }

        private List<OUT> processParallel() {
            List<OUT> elements = new ArrayList<>();

            int processors = Runtime.getRuntime().availableProcessors();
            List<Future<Collection<?>>> futures = new ArrayList<>();
            List<? extends List<?>> partitionedLists = new Partition<>(list, list.size() / processors);

            for (List<?> subList : partitionedLists) {
                futures.add(CompletableFuture.supplyAsync(
                        () -> {
                            List<Object> subElements = new ArrayList<>();
                            for (Object el : subList) {
                                wrapFunctions(subElements::add).accept(el);
                            }

                            return subElements;
                        }));
            }

            CompletableFuture<Void> all = CompletableFuture.allOf(futures.toArray(new CompletableFuture[1]));
            try {
                all.get();
                for (Future<Collection<?>> future : futures) {
                    elements.addAll((Collection<? extends OUT>) future.get());
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            return elements;
        }

        Consumer wrapFunctions(Consumer lastConsumer) {
            Consumer consumer = lastConsumer;
            for (StreamStage stage = this; stage.prevStage != null; stage = stage.prevStage) {
                consumer = (Consumer) stage.consumerPipelineTransformer.apply(consumer);
            }
            return consumer;
        }

        private List<OUT> processSerial() {
            List<OUT> elements = new ArrayList<>();

            Consumer<OUT> finalConsumer = elements::add;

            Consumer listElConsumer = wrapFunctions(finalConsumer);

            for (Object el : list) {
                listElConsumer.accept(el);
            }

            return elements;
        }
    }

    public static <T> SimpleStream<T> stream(List<T> list, boolean parallel) {
        return PerElementTransformStageChainStream.startStage(list, parallel);
    }

    private static <T> SimpleStream<T> startStage(List<T> list, boolean parallel) {
        return new StreamStage<T, T>(
                list,
                new Function<Consumer<T>, Consumer<T>>() {
                    @Override
                    public Consumer<T> apply(Consumer<T> tConsumer) {
                        return new StreamStage.TransformChain<T, T>(tConsumer) {
                            @Override
                            public void accept(T t) {
                                downstream.accept(t);
                            }
                        };
                    }
                },
                parallel);
    }
}
