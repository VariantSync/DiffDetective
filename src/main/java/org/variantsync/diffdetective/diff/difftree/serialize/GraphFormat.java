package org.variantsync.diffdetective.diff.difftree.serialize;

/**
 * Refers to the structure that is represented by a DiffTree.
 * For some purposes, certain diff graphs might also be represented
 * as a DiffTree with an artificial root.
 */
public enum GraphFormat {
    /**
     * A diffgraph has no explicit root.
     */
    DIFFGRAPH,
    /**
     * Default value. Describes a DiffTree that does not model anything other than a DiffTree.
     */
    DIFFTREE
}
