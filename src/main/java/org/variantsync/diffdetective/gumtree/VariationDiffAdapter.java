package org.variantsync.diffdetective.gumtree;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.tree.TreeNode;
import org.variantsync.functjonal.Cast;

/**
 * Adapter for running Gumtree's matching algorithms on the projections of variation diffs.
 *
 * This class is almost identical to {@link VariationTreeAdapter} except that it provides type safe
 * access to the projected {@link DiffNode}.
 */
public class VariationDiffAdapter<L extends Label> extends VariationTreeAdapter<L> {
    public VariationDiffAdapter(DiffNode<L> node, Time time) {
        super(node.projection(time));
    }

    public VariationDiffAdapter(Projection<L> node) {
        super(node);
    }

    protected VariationTreeAdapter<L> newInstance(TreeNode<?, VariationLabel<L>> node) {
        return new VariationDiffAdapter<>(Cast.unchecked(node));
    }

    public DiffNode<L> getDiffNode() {
        return Cast.<TreeNode<?, VariationLabel<L>>, Projection<L>>unchecked(getVariationNode()).getBackingNode();
    }
}
