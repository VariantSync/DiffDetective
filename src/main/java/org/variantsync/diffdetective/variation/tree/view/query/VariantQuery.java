package org.variantsync.diffdetective.variation.tree.view.query;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

public record VariantQuery(Node configuration) implements Query {
    @Override
    public boolean test(VariationNode<?> v) {
        return SAT.isSatisfiable(new And(configuration, v.getPresenceCondition()));
    }

    @Override
    public String parametersToString() {
        return configuration.toString(NodeWriter.logicalSymbols);
    }

    @Override
    public String getFunctionName() {
        return "configure";
    }

    @Override
    public String toString() {
        return Query.toString(this);
    }
}
