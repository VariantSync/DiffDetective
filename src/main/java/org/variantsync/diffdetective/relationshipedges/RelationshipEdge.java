package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class RelationshipEdge<T extends RelationshipType>{
    DiffNode from, to;

    public RelationshipEdge(DiffNode from, DiffNode to) {
        this.from = from;
        this.to = to;
    }
}
