package org.variantsync.diffdetective.variation.diff;

import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

public record ProjectionSource(DiffTree origin, Time time) implements VariationTreeSource {
}
