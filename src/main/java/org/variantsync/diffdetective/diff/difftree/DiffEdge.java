package org.variantsync.diffdetective.diff.difftree;
import java.util.Objects;

/**
 * Returns an edge of two nodes
 */
public class DiffEdge {
    public final DiffNode parent;
    public final DiffNode child;

    public DiffEdge(DiffNode parent, DiffNode child) {
        this.parent = parent;
        this.child = child;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DiffEdge diffEdge = (DiffEdge) o;
        return Objects.equals(parent, diffEdge.parent) && Objects.equals(child, diffEdge.child);
    }

    @Override
    public int hashCode() {
        return Objects.hash(parent, child);
    }
}
