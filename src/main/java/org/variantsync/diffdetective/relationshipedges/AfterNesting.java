package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class AfterNesting implements RelationshipType {
    static AfterNesting instance;

    public static AfterNesting getInstance(){
        return instance;
    }

    public static boolean areInRelation(DiffNode a, DiffNode b) {
        return (a.getAfterParent() == b);
    }
}
