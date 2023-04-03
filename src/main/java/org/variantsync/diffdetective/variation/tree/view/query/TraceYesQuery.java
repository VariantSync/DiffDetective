package org.variantsync.diffdetective.variation.tree.view.query;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.variation.tree.VariationNode;

public record TraceYesQuery(Node configuration) implements Query {
    @Override
    public boolean test(VariationNode<?> variationNode) {
        return SAT.implies(variationNode.getPresenceCondition(), configuration);
    }

    @Override
    public String getName() {
        return "trace_{all}(" + configuration.toString(NodeWriter.logicalSymbols)  + ")";
    }
}
