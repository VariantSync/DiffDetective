package org.variantsync.diffdetective.variation;

import java.util.List;

import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc

/**
 * Base interface for labels of {@link VariationTree}s and {@link VariationDiff}s.
 */
public interface Label {
    List<String> getLines();
    Label clone();
}
