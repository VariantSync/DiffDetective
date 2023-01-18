package org.variantsync.diffdetective.gumtree;

import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.tree.VariationNode;

public class WrappedDiffTree extends WrappedVariationTree {
    public WrappedDiffTree(DiffNode node, Time time) {
        super(node.projection(time));
    }

    public WrappedDiffTree(Projection node) {
        super(node);
    }

    protected WrappedVariationTree newInstance(VariationNode<?> node) {
        return new WrappedDiffTree((Projection)node);
    }

    public DiffNode getDiffNode() {
        return ((Projection)getVariationNode()).getBackingNode();
    }
}
