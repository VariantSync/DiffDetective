package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.function.Predicate;

@FunctionalInterface
public interface Query extends Predicate<VariationTreeNode> {
    default boolean isInteresting(final VariationTreeNode v) {
        return test(v);
    }
}
