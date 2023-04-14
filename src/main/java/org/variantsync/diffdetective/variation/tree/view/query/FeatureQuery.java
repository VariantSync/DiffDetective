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
    public String parametersToString() {
        return featureName();
    }

    @Override
    public String getFunctionName() {
        return "traceall";
    }

    @Override
    public String toString() {
        return Query.toString(this);
    }
}
