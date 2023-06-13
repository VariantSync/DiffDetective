package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffTree;

/**
 * Transformer that relabels the root of a DiffTree.
 * @author Paul Bittner
 */
public class RelabelRoot<L extends Label> implements DiffTreeTransformer<L> {
    private final L newLabel;

    /**
     * Creates a new transformation that will set the root's label
     * of a DiffTree to the given text.
     * @param newLabel New label for the root node.
     */
    public RelabelRoot(final L newLabel) {
        this.newLabel = newLabel;
    }

    @Override
    public void transform(DiffTree<L> diffTree) {
        diffTree.getRoot().setLabel(newLabel);
    }
}
