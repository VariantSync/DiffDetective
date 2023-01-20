package org.variantsync.diffdetective.diff.difftree;

/**
 * A directed edge of between two nodes.
 */
public record DiffEdge(DiffNode parent, DiffNode child) {
}
