package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * This transformation causes bugs. In particular it may invalidate VariationDiffs semantically.
 * For example, it might remove an IF but keep its ELSE branches which is illegal.
 */
@Deprecated
public record FeatureExpressionFilter<L extends Label>(Predicate<DiffNode<L>> isFeatureAnnotation) implements VariationDiffTransformer<L> {
    @Override
    public void transform(VariationDiff<L> variationDiff) {
        final List<DiffNode<L>> illegalNodes = new ArrayList<>();
        variationDiff.forAll(node -> {
            if (node.isAnnotation() && !isFeatureAnnotation.test(node)) {
                illegalNodes.add(node);
            }
        });

        for (final DiffNode<L> illegalAnnotation : illegalNodes) {
            variationDiff.removeNode(illegalAnnotation);
        }
    }
}
