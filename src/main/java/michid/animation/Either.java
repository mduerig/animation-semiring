package michid.animation;

public sealed interface Either<L, R> {
    record Left<L, R>(L l) implements Either<L, R> {}
    record Right<L, R>(R r) implements Either<L, R> {}

    static <L, R> Either<L, R> left(L l) {
        return new Left<>(l);
    }

    static <L, R> Either<L, R> right(R r) {
        return new Right<>(r);
    }

    static <T> T union(Either<T, T> either) {
        return switch (either) {
            case Left<T, T>(T l) -> l;
            case Right<T, T>(T r) -> r;
        };
    }
}
