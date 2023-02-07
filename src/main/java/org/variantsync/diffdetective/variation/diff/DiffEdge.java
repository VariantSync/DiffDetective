package org.variantsync.diffdetective.variation.diff;

/**
 * A directed edge of between two nodes.
 */
public record DiffEdge(DiffNode parent, DiffNode child) {
}
