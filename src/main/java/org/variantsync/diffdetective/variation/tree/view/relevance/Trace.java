package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.List;

import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Relevance predicate that traces a certain feature syntactically within a variation tree.
 * This relevance predicate is the implementation of Equation 6 in our SPLC'23 paper.
 */
public record Trace(String featureName) implements Relevance {
    @Override
    public boolean test(VariationNode<?, ?> v) {
        return v.getPresenceCondition().getUniqueContainedFeatures().stream().anyMatch(
                otherFeatureName -> featureName().equals(otherFeatureName)
        );
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(featureName());
    }

    @Override
    public String getSourceExplanation() {
        return "traceall";
    }

    @Override
    public String toString() {
        return Relevance.toString(this);
    }
}
