package org.variantsync.diffdetective.util;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;

public class CollectionUtils {
    public static <E> E getRandomElement(Random random, Set<E> set) {
        final int index = random.nextInt(set.size());
        final Iterator<E> iter = set.iterator();
        for (int i = 0; i < index; i++) {
            iter.next();
        }
        return iter.next();
    }
}
