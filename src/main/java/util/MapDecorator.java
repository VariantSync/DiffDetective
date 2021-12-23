package util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

public abstract class MapDecorator<K, V> implements Map<K, V> {
    protected final Map<K, V> inner;

    public MapDecorator(final Map<K, V> inner) {
        this.inner = inner;
    }

    public Map<K, V> unwrap() {
        return inner;
    }

    @Override
    public int size() {
        return inner.size();
    }

    @Override
    public boolean isEmpty() {
        return inner.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return inner.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return inner.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return inner.get(key);
    }

    @Override
    public V put(K key, V value) {
        return inner.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return inner.remove(key);
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        inner.putAll(m);
    }

    @Override
    public void clear() {
        inner.clear();
    }

    @Override
    public Set<K> keySet() {
        return inner.keySet();
    }

    @Override
    public Collection<V> values() {
        return inner.values();
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return inner.entrySet();
    }
}
