package util.semigroup;

@FunctionalInterface
public interface Semigroup<T> {
    T append(final T a, final T b);

    static <U> Semigroup<U> selectFirst() { return (a, b) -> a; }

    /**
     * Asserts that any two values to combine are in fact equal and thus always picks the first element.
     * If two values to combine are not equals, throws a SemiGroupCannotAppend exception.
     * @param <U> Type of group.
     * @return A semigroup which can only append equal elements and throws an error when invoked with two different elements.
     */
    static <U> Semigroup<U> assertEquals() {
        return (a, b) -> {
            if (!a.equals(b)) {
                throw new SemigroupCannotAppend("Assertion failed. The following objects where assumed to be equal but are not: \"" + a + "\"; \"" + b + "\"!");
            }
            return a;
        };
    }
}
