package org.variantsync.diffdetective.variation;

import java.util.List;

import org.variantsync.diffdetective.variation.diff.VariationDiff; // For Javadoc
import org.variantsync.diffdetective.variation.tree.VariationTree; // For Javadoc

/**
 * Base interface for labels of {@link VariationTree}s and {@link VariationDiff}s.
 */
public interface Label {
    /**
     * Returns the lines which need to be printed before the children of a node.
     * Most lines associated with a node should be included in this list, for example, {@code #if}s are stored here.
     */
    List<String> getLines();
    /**
     * Returns the lines which need to be printed after the children of a node.
     * For example, {@code #endif}s are stored as trailing lines.
     */
    List<String> getTrailingLines();
    /**
     * Creates a deep copy of this label.
     */
    Label clone();
}
