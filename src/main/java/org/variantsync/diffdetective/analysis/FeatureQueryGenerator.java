package org.variantsync.diffdetective.analysis;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

public class FeatureQueryGenerator {
    public static Set<String> featureQueryGenerator(DiffTree diffTree){
        HashSet<String> allQueries = new HashSet<>();
        diffTree.forAll(n -> {
            Node formula = n.getDirectFeatureMapping();
            if (formula != null) {
                allQueries.addAll(formula.getLiterals().stream().map(literal -> literal.var.toString()).collect(Collectors.toSet()));
            }
        });
        return allQueries;
    }
}
