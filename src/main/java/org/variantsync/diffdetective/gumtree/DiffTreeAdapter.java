package org.variantsync.diffdetective.gumtree;

import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Adapter for running Gumtree's matching algorithms on the projections of variation diffs.
 *
 * This class is almost identical to {@link VariationTreeAdapter} except that it provides type safe
 * access to the projected {@link DiffNode}.
 */
public class DiffTreeAdapter extends VariationTreeAdapter {
    public DiffTreeAdapter(DiffNode node, Time time) {
        super(node.projection(time));
    }

    public DiffTreeAdapter(Projection node) {
        super(node);
    }

    protected VariationTreeAdapter newInstance(VariationNode<?> node) {
        return new DiffTreeAdapter((Projection)node);
    }

    public DiffNode getDiffNode() {
        return ((Projection)getVariationNode()).getBackingNode();
    }
}
