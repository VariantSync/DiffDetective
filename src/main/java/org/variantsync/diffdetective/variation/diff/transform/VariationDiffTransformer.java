package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

/**
 * Interface that represents inplace transformations of VariationDiffs.
 * A VariationDiffTransformer is intended to alter a given VariationDiff.
 * @author Paul Bittner
 */
public interface VariationDiffTransformer<L extends Label> extends Transformer<VariationDiff<L>> {
}
