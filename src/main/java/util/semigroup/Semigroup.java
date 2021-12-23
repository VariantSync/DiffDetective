package util.semigroup;

public interface Semigroup<T> {
    void append(T other);

    static <U> Semigroup<U> singleton(U u) {
        return new SingletonSemigroup<>(u);
    }
}
