package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

public record FeatureQuery(String featureName) implements Query {
    @Override
    public boolean test(VariationTreeNode variationTreeNode) {
        return variationTreeNode.getPresenceCondition().getUniqueContainedFeatures().stream().anyMatch(
                otherFeatureName -> featureName().equals(otherFeatureName)
        );
    }

    @Override
    public String getName() {
        return "feature(" + featureName() + ")";
    }
}
