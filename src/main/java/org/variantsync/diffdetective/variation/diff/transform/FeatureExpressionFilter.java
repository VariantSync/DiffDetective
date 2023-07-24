package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This transformation causes bugs. In particular it may invalidate DiffTrees semantically.
 * For example, it might remove an IF but keep its ELSE branches which is illegal.
 */
@Deprecated
public record FeatureExpressionFilter<L extends Label>(Predicate<DiffNode<L>> isFeatureAnnotation) implements DiffTreeTransformer<L> {
    @Override
    public void transform(DiffTree<L> diffTree) {
        final List<DiffNode<L>> illegalNodes = new ArrayList<>();
        diffTree.forAll(node -> {
            if (node.isAnnotation() && !isFeatureAnnotation.test(node)) {
                illegalNodes.add(node);
            }
        });

        for (final DiffNode<L> illegalAnnotation : illegalNodes) {
            diffTree.removeNode(illegalAnnotation);
        }
    }
}
