package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.variantsync.diffdetective.variation.tree.VariationNode;

public record Search(String artifact) implements Relevance {
    @Override
    public boolean test(VariationNode<?> v) {
        if (v.isArtifact()) {
            return v.getLabelLines().contains(artifact);
        }

        return false;
    }

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