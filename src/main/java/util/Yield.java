package util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

public class Yield<T> implements Iterator<T>, Iterable<T> {
    private T focus = null;
    boolean focusVisited = true;
    private final Supplier<T> getNext;

    public Yield(Supplier<T> getNext) {
        this.getNext = getNext;
    }

    public <U> Yield<U> map(final Function<T, U> f) {
        return new Yield<>(() -> f.apply(getNext.get()));
    }

    synchronized void updateFocus() {
        if (focusVisited) {
            focus = getNext.get();
            focusVisited = false;
        }
    }

    @Override
    public synchronized boolean hasNext() {
        updateFocus();
        return focus != null;
    }

    @Override
    public synchronized T next() {
        updateFocus();
        focusVisited = true;
        return focus;
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }

    public synchronized T nextSynchronized() {
        if (hasNext()) {
            return next();
        }
        return null;
    }

    public List<T> toList() {
        final List<T> l = new ArrayList<>();
        T val = next();
        while (val != null) {
            l.add(val);
            val = next();
        }
        return l;
    }
}