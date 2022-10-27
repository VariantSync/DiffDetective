package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class RelationshipEdge<T extends RelationshipType>{
    DiffNode from, to;

    private final Class<T> relationshipType;

    public RelationshipEdge(Class<T> type, DiffNode from, DiffNode to) {
        this.from = from;
        this.to = to;
        this.relationshipType = type;
    }

    public DiffNode getFrom() {
        return from;
    }

    public DiffNode getTo() {
        return to;
    }

    public Class<T> getType(){
        return this.relationshipType;
    }
}
