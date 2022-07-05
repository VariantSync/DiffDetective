package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffTree;

/**
 * Transformer that relabels the root of a DiffTree.
 * @author Paul Bittner
 */
public class RelabelRoot implements DiffTreeTransformer {
    private final String newLabel;

    /**
     * Creates a new transformation that will set the root's label
     * of a DiffTree to the given text.
     * @param newLabel New label for the root node.
     */
    public RelabelRoot(final String newLabel) {
        this.newLabel = newLabel;
    }

    @Override
    public void transform(DiffTree diffTree) {
        diffTree.getRoot().setLabel(newLabel);
    }
}
