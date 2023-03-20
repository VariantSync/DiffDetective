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

    @Override
    public String getName() {
        return "artifact(" + artifact() + ")";
    }
}