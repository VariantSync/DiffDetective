package util.iterator;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class ClusteredIterator<T> implements Iterator<List<T>> {
    private final Iterator<T> inner;
    private final int clusterSize;

    public ClusteredIterator(final Iterator<T> inner, int clusterSize) {
        this.inner = inner;
        this.clusterSize = clusterSize;
    }

    @Override
    public boolean hasNext() {
        return inner.hasNext();
    }

    @Override
    public List<T> next() {
        final List<T> cluster = new ArrayList<>(clusterSize);
        for (int i = 0; i < clusterSize && inner.hasNext(); ++i) {
            cluster.add(inner.next());
        }

        return cluster;
    }
}
