package util.semigroup;

import util.MapDecorator;

import java.util.Map;
import java.util.function.Function;

/**
 * A map whose values are merged instead of overwritten upon put with a duplicate key.
 * In particular, when invoking put(k, v) and k is already associated to a value w, then
 * v will be appended to w via w.append(v).
 * @param <K> key type
 * @param <V> value type
 */
public class MergeMap<K, V> extends MapDecorator<K, V> {
    public final InlineSemigroup<MergeMap<K, V>> ISEMIGROUP = MergeMap::append;

    private final Function<V, Semigroup<V>> semigroupFactory;

    public MergeMap(final Map<K, V> inner, final Function<V, Semigroup<V>> semigroupFactory) {
        super(inner);
        this.semigroupFactory = semigroupFactory;
    }

    public MergeMap(final Map<K, V> inner, final Semigroup<V> semigroup) {
        super(inner);
        this.semigroupFactory = x -> semigroup;
    }

    @Override
    public V put(final K key, final V value) {
        V result = value;
        if (containsKey(key)) {
            result = get(key);
            result = semigroupFactory.apply(result).append(result, value);
        }
        super.put(key, result);
        return result;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.isEmpty()) return;
        for (var entry : m.entrySet()) {
            put(entry.getKey(), entry.getValue());
        }
    }

    public void append(Map<K, V> other) {
        putAll(other);
    }

    /**
     * Appends the given value to the value currently registered in the given map for the given key.
     * If the given map does not contain an entry for the given key, the given value is stored as
     * the keys value instead.
     * @param <K> key type
     * @param <V> value type
     * @param map The map to which the value should be added.
     * @param key The key whose value should be appended by the given value. If the key is not contained
*            in the given map, a new entry is created with this key and the given value.
     * @param valueToAppend The value to append to map.get(key). If the map does not contain an entry for
*                      the given key, a new entry with the given key value pair is made (i.e., map.put(key, valueToAppend)).
     */
    public static <K, V> void putValue(
            final Map<K, V> map,
            final K key,
            final V valueToAppend,
            Semigroup<V> semigroup)
    {
        V result = valueToAppend;
        if (map.containsKey(key)) {
            result = semigroup.append(map.get(key), valueToAppend);
        }
        map.put(key, result);
    }

    public static <K, V> void putAllValues(
            final Map<K, V> map,
            final Map<K, V> other,
            Semigroup<V> semigroup)
    {
        for (final Map.Entry<K, V> otherEntry : other.entrySet()) {
            putValue(map, otherEntry.getKey(), otherEntry.getValue(), semigroup);
        }
    }
}
