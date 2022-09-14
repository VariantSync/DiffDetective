package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class UnchangedNesting implements RelationshipType{
    static UnchangedNesting instance;

    public static UnchangedNesting getInstance(){
        return instance;
    }

    public static boolean areInRelation(DiffNode a, DiffNode b) {
        return (a.getBeforeParent() == b && a.getAfterParent() == b);
    }
}
