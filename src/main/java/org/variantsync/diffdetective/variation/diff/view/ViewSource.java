package org.variantsync.diffdetective.variation.diff.view;

import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

/**
 * A {@link DiffTreeSource} that remembers that a variation diff represents a view on
 * another variation diff.
 * @param diff The original variation diff on which the variation diff with this source is a view on.
 * @param relevance The relevance predicate that was used to create the view.
 */
public record ViewSource(DiffTree diff, Relevance relevance) implements DiffTreeSource {
}
