package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;

public class EdgeTypedTreeDiff<T extends RelationshipType> {
    DiffTree diffTree;
    List<RelationshipEdge<T>> edges;

    public EdgeTypedTreeDiff(DiffTree diffTree, ArrayList<RelationshipEdge<T>> edges) {
        this.diffTree = diffTree;
        this.edges = edges;
    }
}
