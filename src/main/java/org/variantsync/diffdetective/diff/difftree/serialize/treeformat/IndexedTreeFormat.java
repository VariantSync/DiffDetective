package org.variantsync.diffdetective.diff.difftree.serialize.treeformat;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

/**
 * Exports tree by indexing them.
 * This format keeps an internal counter that is incremented on each call of {@link #toLabel(DiffTreeSource)}.
 * Thus, every produced label will have the successive index of the previously produced label.
 */
public class IndexedTreeFormat implements DiffTreeLabelFormat {
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
    public DiffTreeSource fromLabel(String label) {
        return DiffTreeSource.Unknown;
    }

    @Override
    public String toLabel(DiffTreeSource diffTreeSource) {
        final String result = "" + nextId;
        ++nextId;
        return result;
    }
}
