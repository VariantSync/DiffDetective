package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record FromVariationTreeSource(VariationTreeSource inner) implements VariationDiffSource {
    @Override
    public String toString() {
        return inner.toString();
    }
}
