package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.function.Predicate;

public interface Query extends Predicate<VariationNode<?>> {
    String getFunctionName();
    String parametersToString();

    static String toString(Query q) {
        return q.getFunctionName() + "(" + q.parametersToString() + ")";
    }
}
