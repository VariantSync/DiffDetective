package util.semigroup;

@FunctionalInterface
public interface InlineSemigroup<T> extends Semigroup<T> {
    void appendToFirst(final T a, final T b);

    default T append(final T a, final T b) {
        appendToFirst(a, b);
        return a;
    }
}
