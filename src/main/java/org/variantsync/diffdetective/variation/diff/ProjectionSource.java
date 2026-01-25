package org.variantsync.diffdetective.variation.diff;

import java.util.List;

import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.Label;

public record ProjectionSource<L extends Label>(VariationDiff<L> origin, Time time) implements Source {
    @Override
    public String getSourceExplanation() {
        return "Projection";
    }

    @Override
    public List<Source> getSources() {
        return List.of(origin);
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(time);
    }
}
