package org.variantsync.diffdetective.variation;

import org.variantsync.diffdetective.variation.tree.VariationNode;

record Variant<L extends Label>(
    TreeNode<?, L> root,
    VariationTreeSource source
) {
}
