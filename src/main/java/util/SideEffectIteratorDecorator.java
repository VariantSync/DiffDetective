package util;

import java.util.Iterator;
import java.util.function.Consumer;

public class SideEffectIteratorDecorator<T> implements Iterator<T> {
    private final Iterator<T> inner;
    private final Consumer<T> effect;

    public SideEffectIteratorDecorator(final Iterator<T> inner, final Consumer<T> effect) {
        this.inner = inner;
        this.effect = effect;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public T next() {
        final T n = inner.next();
        effect.accept(n);
        return n;
    }
}
