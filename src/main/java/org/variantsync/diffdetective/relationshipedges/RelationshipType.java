package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public interface RelationshipType {
    static boolean areInRelation(DiffNode a, DiffNode b) {
        return false;
    }
}
