package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record BadVDiffFromVariationDiffSource(VariationDiffSource initialVariationDiff) implements VariationTreeSource {
    @Override
    public String toString() {
        return initialVariationDiff.toString();
    }
}
