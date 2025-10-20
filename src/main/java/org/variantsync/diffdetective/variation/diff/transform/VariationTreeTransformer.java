package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.tree.VariationTree;

/**
 * Interface that represents inplace transformations of VariationTrees.
 * A VariationTreeTransformer is intended to alter a given VariationTree.
 * @author Paul Bittner
 */
public interface VariationTreeTransformer<L extends Label> extends Transformer<VariationTree<L>> {
}
