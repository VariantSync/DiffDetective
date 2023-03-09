package org.variantsync.diffdetective.variation.diff.source;

import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record FromVariationTreeSource(VariationTreeSource inner) implements DiffTreeSource {
    @Override
    public String toString() {
        return inner.toString();
    }
}
