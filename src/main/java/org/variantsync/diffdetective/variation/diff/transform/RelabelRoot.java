package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

/**
 * Transformer that relabels the root of a VariationDiff.
 * @author Paul Bittner
 */
public class RelabelRoot<L extends Label> implements VariationDiffTransformer<L> {
    private final L newLabel;

    /**
     * Creates a new transformation that will set the root's label
     * of a VariationDiff to the given text.
     * @param newLabel New label for the root node.
     */
    public RelabelRoot(final L newLabel) {
        this.newLabel = newLabel;
    }

    @Override
    public void transform(VariationDiff<L> variationDiff) {
        variationDiff.getRoot().setLabel(newLabel);
    }
}
