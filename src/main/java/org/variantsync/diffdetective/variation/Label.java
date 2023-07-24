package org.variantsync.diffdetective.variation;

import java.util.List;

import org.variantsync.diffdetective.variation.diff.DiffTree; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc

/**
 * Base interface for labels of {@link VariationTree}s and {@link DiffTree}s.
 */
public interface Label {
    List<String> getLines();
    Label clone();
}
