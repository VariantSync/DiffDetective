package util.iterator;

import java.util.Iterator;
import java.util.function.Function;

public class MappedIterator<A, B> implements Iterator<B> {
    private final Iterator<A> as;
    private final Function<A, B> map;

    public MappedIterator(Iterator<A> iterator, Function<A, B> map) {
        this.as = iterator;
        this.map = map;
    }

    @Override
    public boolean hasNext() {
        return as.hasNext();
    }

    @Override
    public B next() {
        return map.apply(as.next());
    }
}
