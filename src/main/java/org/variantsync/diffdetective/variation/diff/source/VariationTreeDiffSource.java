package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.variation.diff.DiffTree; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

/**
 * Describes that a {@link DiffTree} was created from two {@link VariationTree}s.
 */
public record VariationTreeDiffSource(
    VariationTreeSource before,
    VariationTreeSource after
) implements DiffTreeSource {
}
