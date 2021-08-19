package util;

import java.util.Iterator;
import java.util.function.Supplier;

public class Yield<T> implements Iterator<T>, Iterable<T> {
    private T focus = null;
    boolean focusVisited = true;
    private final Supplier<T> getNext;

    public Yield(Supplier<T> getNext) {
        this.getNext = getNext;
    }

    void updateFocus() {
        if (focusVisited) {
            focus = getNext.get();
            focusVisited = false;
        }
    }

    @Override
    public boolean hasNext() {
        updateFocus();
        return focus != null;
    }

    @Override
    public T next() {
        updateFocus();
        focusVisited = true;
        return focus;
    }

    @Override
    public Iterator<T> iterator() {
        return this;
    }
}