package org.variantsync.diffdetective.variation.diff;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record ProjectionSource<L extends Label>(VariationDiff<L> origin, Time time) implements VariationTreeSource {
}
