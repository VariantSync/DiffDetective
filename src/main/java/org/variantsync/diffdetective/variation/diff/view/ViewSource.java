package org.variantsync.diffdetective.variation.diff.view;

import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

public record ViewSource(DiffTree diff, Query q) implements DiffTreeSource {
}
