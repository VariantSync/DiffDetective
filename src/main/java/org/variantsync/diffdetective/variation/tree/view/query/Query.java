package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.function.Predicate;

@FunctionalInterface
public interface Query extends Predicate<VariationNode<?>> {
    default boolean isInteresting(final VariationNode<?> v) {
        return test(v);
    }

    default String getName() {
        return getClass().getSimpleName();
    }
}
