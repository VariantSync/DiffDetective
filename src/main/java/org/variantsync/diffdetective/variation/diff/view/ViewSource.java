package org.variantsync.diffdetective.variation.diff.view;

import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

public record ViewSource(DiffTree diff, Relevance relevance) implements DiffTreeSource {
}
