package michid.animation;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.round;
import static michid.animation.Either.left;
import static michid.animation.Either.right;

import java.util.function.Consumer;
import java.util.function.Function;

public sealed interface Animation<T> {
    record Cancelled<T>() implements Animation<T> {}
    record Trivial<T>() implements Animation<T> {}
    record Show<T>(double duration, Function<Double, T> value) implements Animation<T> {}

    default Animation<Void> bind(Consumer<T> renderer) {
        return switch(this) {
            case Cancelled() -> new Cancelled<>();
            case Trivial() -> new Trivial<>();
            case Show(var duration, var value) ->
                new Show<>(duration, progress -> {
                    renderer.accept(value.apply(progress));
                    return null;
                });
        };
    }

    default void render(double at) {
        switch (this) {
            case Show(var duration, var value) -> value.apply(at);
            default -> {}
        }
    }

    static Animation<Long> linear(double duration, long from, long to) {
        return new Show<>(duration, progress -> round(((1 - progress) * from) + (progress * to)));
    }

    static <T> Animation<T> constant(double duration, T value) {
        return new Show<>(duration, progress -> value);
    }

    default Animation<T> reverse() {
        return switch (this) {
            case Show(var duration, var value) ->
                new Show<>(duration, progress -> value.apply(1 - progress));
            default -> this;
        };
    }

    default Animation<T> repeat(int count) {
        return switch (this) {
            case Show(var duration, var value) ->
                new Show<>(count * duration, progress ->
                    value.apply(count * progress % 1));
            default -> this;
        };
    }

    default Animation<T> loop() {
        return switch (this) {
            case Show<T> s ->
                seq(s, s.reverse())
                .map(Either::union);
            default -> this;
        };
    }

    default Animation<T> ease(Function<Double, Double> easing) {
        return switch (this) {
            case Show(var duration, var value) ->
                new Show<>(duration, progress -> value.apply(easing.apply(progress)));
            default -> this;
        };
    }

    default Animation<T> delay(double delay) {
        return switch (this) {
            case Show(var __, var value) ->
                seq(constant(delay, value.apply(0.0)), this)
                    .map(Either::union);
            default -> this;
        };
    }

    default <R> Animation<R> map(Function<T, R> f) {
        return switch(this) {
            case Cancelled() -> new Cancelled<>();
            case Trivial() -> new Trivial<>();
            case Show(var duration, var value) ->
                new Show<>(duration, progress ->
                    f.apply((value.apply(progress))));
        };
    }

    static <S, T> Animation<Either<S, T>> seq(Animation<S> x, Animation<T> y) {
        return switch (new Pair<>(x, y)) {
            case Pair(Cancelled(), Animation a) -> new Cancelled<>();

            case Pair(Animation a, Cancelled()) -> new Cancelled<>();

            case Pair(Trivial t, Animation a) -> a.map(Either::right);

            case Pair(Animation a, Trivial t) -> a.map(Either::left);

            case Pair(Show(var duration1, var value1), Show(var duration2, var value2)) ->
                    new Show<>(duration1 + duration2, progress -> {
                        var ratio = duration1 / (duration1 + duration2);
                        if (progress < ratio) {
                            return left(value1.apply(progress / ratio));
                        } else {
                            return right(value2.apply((progress - ratio) / (1 - ratio)));
                        }
                    });
        };
    }

    static <S, T> Animation<Pair<S, T>> par(Animation<S> x, Animation<T> y) {
        return switch (new Pair<>(x, y)) {
            case Pair(Cancelled(), Trivial()) -> new Trivial<>();

            case Pair(Trivial(), Cancelled()) -> new Trivial<>();

            case Pair(Trivial(), Trivial()) -> new Trivial<>();

            case Pair(Cancelled(), Cancelled()) -> new Cancelled<>();

            case Pair(Show(var duration1, var value1), Show(var duration2, var value2)) -> {
                    var duration = max(duration1, duration2);
                    yield new Show<>(duration, progress -> new Pair<>(
                        value1.apply(min(1, progress * duration / duration1)),
                        value2.apply(min(1, progress * duration / duration2))));
                }

            case Pair(Animation a, Show(var duration, var value)) ->
                    new Show<>(duration, progress -> new Pair<>(null, value.apply(progress)));

            case Pair(Show(var duration, var value), Animation a) ->
                    new Show<>(duration, progress -> new Pair<>(value.apply(progress), null));
        };
    }

    static Semiring<Animation<?>> semiring() {
        return new Semiring<>() {
            @Override
            public Animation<?> zero() {
                return new Cancelled<>();
            }

            @Override
            public Animation<?> one() {
                return new Trivial<>();
            }

            @Override
            public Animation<?> mul(Animation<?> x, Animation<?> y) {
                return seq(x, y);
            }

            @Override
            public Animation<?> add(Animation<?> x, Animation<?> y) {
                return par(x, y);
            }
        };
    }
}
