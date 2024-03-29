package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Relevance predicate that traces a certain feature semantically within a variation tree.
 * This relevance predicate is the implementation of {@code trace_{\subseteq} } in our SPLC'23 paper.
 */
public record TraceSup(Node configuration) implements Relevance {
    @Override
    public boolean test(VariationNode<?, ?> variationNode) {
        return SAT.implies(variationNode.getPresenceCondition(), configuration);
    }

    @Override
    public String parametersToString() {
        return configuration.toString(NodeWriter.logicalSymbols);
    }

    @Override
    public String getFunctionName() {
        return "traceyes";
    }

    @Override
    public String toString() {
        return Relevance.toString(this);
    }
}
