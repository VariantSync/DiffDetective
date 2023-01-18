package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

/**
 * Describes that a {@link DiffTree} was created from two {@link VarationTree}s.
 */
public record VariationTreeDiffSource(
    VariationTreeSource before,
    VariationTreeSource after
) implements DiffTreeSource {
}
