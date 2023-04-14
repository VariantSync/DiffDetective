package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationNode;

public record ArtifactQuery(String artifact) implements Query {
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
        return Query.toString(this);
    }
}