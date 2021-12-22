package util.functional;

record SingletonSemigroup<T>(T element) implements Semigroup<T> {
    @Override
    public void append(T other) {
        if (!equals(other)) {
            throw new SingletonSemigroupAppendException(this, other);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final SingletonSemigroup<?> that = (SingletonSemigroup<?>) o;
        return element.equals(that.element);
    }

    @Override
    public int hashCode() {
        return element.hashCode();
    }

    @Override
    public String toString() {
        return element.toString();
    }
}
