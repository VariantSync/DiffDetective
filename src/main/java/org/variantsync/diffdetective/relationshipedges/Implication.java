package org.variantsync.diffdetective.relationshipedges;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class Implication implements RelationshipType {
    static Implication instance;

    public static Implication getInstance(){
        return instance;
    }

    public static boolean areInRelation(DiffNode a, DiffNode b) {
        if(!a.isIf() || !b.isIf()){ // TODO: check for all macro nodes
            return false;
        }
        Node featureMappingA = a.getDirectFeatureMapping();
        Node featureMappingB = b.getDirectFeatureMapping();
        if(featureMappingA == null || featureMappingB == null) return false; //TODO: this is true for macro nodes #ELSE and #ENDIF
        return SAT.implies(featureMappingA, featureMappingB);
    }
}
