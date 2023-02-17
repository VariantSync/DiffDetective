package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record BadVDiffFromDiffTreeSource(DiffTreeSource s) implements VariationTreeSource {
    @Override
    public String toString() {
        return s.toString();
    }
}
