package org.variantsync.diffdetective.relationshipedges;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;

public class Implication implements RelationshipType {

    public static boolean areInRelation(DiffNode a, DiffNode b) {
        if(!a.isMacro() || !b.isMacro()){
            return false;
        }
        Node featureMappingA = a.getDirectFeatureMapping();
        Node featureMappingB = b.getDirectFeatureMapping();
        if(featureMappingA == null || featureMappingB == null) return false; //TODO: this is true for macro nodes #ELSE and #ENDIF
        return SAT.implies(featureMappingA, featureMappingB);
    }
}
