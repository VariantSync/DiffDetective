package util.functional;

import org.pmw.tinylog.Logger;
import util.MapDecorator;
import util.Util;

import java.util.Map;

/**
 * A map whose values decide how to handle collisions.
 * In particular, when invoking put(k, v) and k is already associated to a value w, then
 * v will be appended to w via w.append(v).
 * @param <K> key type
 * @param <V> value type
 */
public class CollisionMap<K, V extends Semigroup<?>> extends MapDecorator<K, V> implements Semigroup<Map<K, V>> {
    private static class Collision extends RuntimeException {
        Collision(String reason) { super(reason); }
    }

    public CollisionMap(final Map<K, V> inner) {
        super(inner);
    }

    @Override
    public V put(final K key, final V value) {
        try {
            return tryPutValue(inner, key, value);
        } catch (SemigroupAppendException e) {
            final String message = "Tried to overwrite value \"" + inner.get(key) + "\" of key \"" + key + "\" with different value \"" + value + "\"!";
            Logger.error(message);
            throw new Collision(message + "; Reason: " + e);
        }
    }

    @Override
    public void append(Map<K, V> other) {
        putAll(other);
    }

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
    public static <K, V extends Semigroup<V>> V putValue(
            final Map<K, V> map,
            final K key,
            final V valueToAppend)
    {
        if (map.containsKey(key)) {
            final V val = map.get(key);
            val.append(valueToAppend);
            return val;
        } else {
            map.put(key, valueToAppend);
            return valueToAppend;
        }
    }

    static <K, V extends Semigroup<?>> V tryPutValue(
            final Map<K, V> map,
            final K key,
            final V valueToAppend) throws ClassCastException
    {
        if (map.containsKey(key)) {
            final V me = map.get(key);
            Util.<V, Semigroup<V>>cast(me).append(valueToAppend);
            return me;
        } else {
            map.put(key, valueToAppend);
            return valueToAppend;
        }
    }
}
