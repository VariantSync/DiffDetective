package util;

import java.util.Map;

public interface Semigroup<T> {
    void append(T other);

    /**
     * Appends the given value to the value currently registered in the given map for the given key.
     * If the given map does not contain an entry for the given key, the given value is stored as
     * the keys value instead.
     * @param map The map to which the value should be added.
     * @param key The key whose value should be appended by the given value. If the key is not contained
     *            in the given map, a new entry is created with this key and the given value.
     * @param valueToAppend The value to append to map.get(key). If the map does not contain an entry for
     *                      the given key, a new entry with the given key value pair is made (i.e., map.put(key, valueToAppend)).
     * @param <K> key type
     * @param <V> value type
     */
    static <K, V extends Semigroup<V>> void appendValue(
            final Map<K, V> map,
            final K key,
            final V valueToAppend)
    {
        if (map.containsKey(key)) {
            map.get(key).append(valueToAppend);
        } else {
            map.put(key, valueToAppend);
        }
    }
}
