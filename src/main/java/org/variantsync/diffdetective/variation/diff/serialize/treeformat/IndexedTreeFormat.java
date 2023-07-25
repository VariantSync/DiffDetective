package org.variantsync.diffdetective.variation.diff.serialize.treeformat;

import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

/**
 * Exports tree by indexing them.
 * This format keeps an internal counter that is incremented on each call of {@link #toLabel(VariationDiffSource)}.
 * Thus, every produced label will have the successive index of the previously produced label.
 */
public class IndexedTreeFormat implements VariationDiffLabelFormat {
    private int nextId = 0;

    /**
     * Creates a new format starting with index 0.
     */
    public IndexedTreeFormat() {
        reset();
    }

    /**
     * Resets the current index to 0.
     */
    public void reset() {
        nextId = 0;
    }

    @Override
    public VariationDiffSource fromLabel(String label) {
        return VariationDiffSource.Unknown;
    }

    @Override
    public String toLabel(VariationDiffSource variationDiffSource) {
        final String result = "" + nextId;
        ++nextId;
        return result;
    }
}
