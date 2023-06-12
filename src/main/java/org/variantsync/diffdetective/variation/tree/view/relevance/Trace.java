package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Relevance predicate that traces a certain feature syntactically within a variation tree.
 * This relevance predicate is the implementation of Equation 6 in our SPLC'23 paper.
 */
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
