package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.variantsync.diffdetective.variation.tree.VariationNode;

public record Trace(String featureName) implements Relevance {
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
        return Relevance.toString(this);
    }
}
