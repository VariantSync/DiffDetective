package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Relevance predicate that searches for implementation artifacts in a variation tree.
 * This relevance predicate is the implementation of Equation 7 in our SPLC'23 paper.
 */
public record Search(String artifact) implements Relevance {
    @Override
    public boolean test(VariationNode<?, ?> v) {
        if (v.isArtifact()) {
            return v.getLabel().getLines().contains(artifact);
        }

        return false;
    }

    @Override
    public String parametersToString() {
        return artifact();
    }

    @Override
    public String getFunctionName() {
        return "is";
    }

    @Override
    public String toString() {
        return Relevance.toString(this);
    }
}
