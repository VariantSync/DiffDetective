package org.variantsync.diffdetective.diff.difftree.serialize;

/**
 * Root interface for any formats describing content's structure in a linegraph file.
 */
public interface LinegraphFormat {
    /**
     * Name of the format that uniquely identifies the format.
     */
    default String getName() {
        return this.getClass().getName();
    }
}
