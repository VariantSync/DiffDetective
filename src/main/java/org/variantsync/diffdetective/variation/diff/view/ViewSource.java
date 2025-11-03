package org.variantsync.diffdetective.variation.diff.view;

import java.util.List;

import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

/**
 * A {@link Source} that remembers that a variation diff represents a view on another variation
 * diff.
 * @param diff The original variation diff on which the variation diff with this source is a view on.
 * @param relevance The relevance predicate that was used to create the view.
 */
public record ViewSource<L extends Label>(VariationDiff<L> diff, Relevance relevance, String method) implements Source {
    @Override
    public String getSourceExplanation() {
        return "view";
    }

    @Override
    public List<Source> getSources() {
        return List.of(diff);
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of(relevance);
    }
}
