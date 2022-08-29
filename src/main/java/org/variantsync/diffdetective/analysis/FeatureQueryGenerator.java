package org.variantsync.diffdetective.analysis;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.HashSet;
import java.util.Set;

public class FeatureQueryGenerator {
    public static Set<Literal> featureQueryGenerator(DiffTree diffTree){
        HashSet<Literal> allQueries = new HashSet<>();
        diffTree.forAll(n -> {
            Node formula = n.getDirectFeatureMapping();
            if (formula != null) {
                allQueries.addAll(formula.getLiterals());
            }
        });
        return allQueries;
    }
}
