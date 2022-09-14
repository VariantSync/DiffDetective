package org.variantsync.diffdetective.relationshipedges;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class BeforeNesting  implements RelationshipType {
    static BeforeNesting instance;

    public static BeforeNesting getInstance(){
        return instance;
    }

    public static boolean areInRelation(DiffNode a, DiffNode b) {
        return (a.getBeforeParent() == b);
    }
}

