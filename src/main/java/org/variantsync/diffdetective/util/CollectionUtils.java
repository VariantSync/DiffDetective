package org.variantsync.diffdetective.util;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.function.Supplier;

public class CollectionUtils {
    public static <E> E getRandomElement(Random random, Set<E> set) {
        final int index = random.nextInt(set.size());
        final Iterator<E> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }

    public static <K, V> Map<V, K> invert(final Map<K, V> map, final Supplier<Map<V, K>> mapFactory) {
        final Map<V, K> inv = mapFactory.get();

        for (Map.Entry<K, V> entry : map.entrySet()) {
            Assert.assertTrue(
                    inv.putIfAbsent(entry.getValue(), entry.getKey()) == null,
                    "Given map is not invertible!"
            );
        }

        return inv;
    }
}
