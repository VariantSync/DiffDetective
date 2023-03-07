package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

public record ArtifactQuery(String artifact) implements Query {
    @Override
    public boolean test(VariationTreeNode variationTreeNode) {
        if (variationTreeNode.isArtifact()) {
            return variationTreeNode.getLabelLines().contains(artifact);
        }

        return false;
    }

    @Override
    public String getName() {
        return "artifact(" + artifact() + ")";
    }
}