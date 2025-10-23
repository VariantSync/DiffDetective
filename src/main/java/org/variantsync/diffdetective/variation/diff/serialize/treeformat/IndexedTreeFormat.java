package org.variantsync.diffdetective.variation.diff.serialize.treeformat;

import org.variantsync.diffdetective.util.Source;

/**
 * Exports tree by indexing them.
 * This format keeps an internal counter that is incremented on each call of {@link #toLabel(Source)}.
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
    public Source fromLabel(String label) {
        return Source.Unknown;
    }

    @Override
    public String toLabel(Source variationDiffSource) {
        final String result = "" + nextId;
        ++nextId;
        return result;
    }
}
