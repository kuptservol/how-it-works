package ru.skuptsov.stream.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import ru.skuptsov.stream.SimpleStream;

public class PerElementTransformStageChainStream {

    private static class StreamStage<IN, OUT> implements SimpleStream<OUT> {
        private final List<?> list;
        private final StreamStage firstStage;
        private final StreamStage prevStage;
        private final Function<Consumer<OUT>, Consumer<IN>> consumerPipelineWrapper;

        private StreamStage(List<?> list, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineWrapper) {
            this.list = list;
            this.firstStage = this;
            this.prevStage = null;
            this.consumerPipelineWrapper = consumerPipelineWrapper;
        }

        private StreamStage(List<?> list, StreamStage<?, ?> upStream, Function<Consumer<OUT>, Consumer<IN>> consumerPipelineWrapper) {
            this.list = list;
            this.firstStage = upStream.firstStage;
            this.prevStage = upStream;
            this.consumerPipelineWrapper = consumerPipelineWrapper;
        }

        private abstract static class TransformChain<T, OUT> implements Consumer<T> {
            protected final Consumer<? super OUT> downstream;

            private TransformChain(Consumer<? super OUT> downstream) {
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
                    }
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
                    }
            );
        }

        @Override
        @SuppressWarnings("unchecked")
        public List<OUT> collectToList() {
            List<OUT> elements = new ArrayList<>();

            Consumer<OUT> finalConsumer = new Consumer<OUT>() {
                @Override
                public void accept(OUT out) {
                    elements.add(out);
                }
            };

            Consumer listElConsumer = finalConsumer;
            for (StreamStage stage = this; stage.prevStage != null; stage = stage.prevStage) {
                listElConsumer = (Consumer) stage.consumerPipelineWrapper.apply(listElConsumer);
            }

            for (Object el : list) {
                listElConsumer.accept(el);
            }

            return elements;
        }
    }

    public static <T> SimpleStream<T> stream(List<T> list) {
        return PerElementTransformStageChainStream.startStage(list);
    }

    private static <T> SimpleStream<T> startStage(List<T> list) {
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
                });
    }
}
