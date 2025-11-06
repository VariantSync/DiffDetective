package org.variantsync.diffdetective.variation.diff.view;

import java.util.List;

import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

/**
 * A {@link Source} that remembers that a variation tree or diff represents a view on another variation
 * tree or diff.
 * @param target The original variation tree/diff on which the view was created.
 * @param relevance The relevance predicate that was used to create the view.
 */
public record ViewSource(Source target, Relevance relevance, String method) implements Source {
    @Override
    public String getSourceExplanation() {
        return "view";
    }

    @Override
    public List<Source> getSources() {
        return List.of(relevance, target);
    }

    @Override
    public List<Object> getSourceArguments() {
        return List.of();
    }
}
