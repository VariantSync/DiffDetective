package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationNode;

public record FeatureQuery(String featureName) implements Query {
    @Override
    public boolean test(VariationNode<?> v) {
        return v.getPresenceCondition().getUniqueContainedFeatures().stream().anyMatch(
                otherFeatureName -> featureName().equals(otherFeatureName)
        );
    }

    @Override
    public String getName() {
        return "feature(" + featureName() + ")";
    }
}
