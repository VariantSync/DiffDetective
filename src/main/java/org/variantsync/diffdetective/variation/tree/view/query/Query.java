package org.variantsync.diffdetective.variation.tree.view.query;

import org.variantsync.diffdetective.variation.tree.VariationNode;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface Query extends Predicate<VariationNode<?>> {
    String getFunctionName();
    String parametersToString();

    default <TreeNode extends VariationNode<TreeNode>> void computeViewNodes(final TreeNode v, final Consumer<TreeNode> markRelevant) {
        computeViewNodesCheckAll(this, v, markRelevant);
    }

    static <TreeNode extends VariationNode<TreeNode>> void computeViewNodesCheckAll(final Query q, final TreeNode v, final Consumer<TreeNode> markRelevant) {
        for (final TreeNode c : v.getChildren()) {
            if (q.test(c)) {
                c.forMeAndMyAncestors(markRelevant);
            }

            computeViewNodesCheckAll(q, c, markRelevant);
        }
    }

    static String toString(Query q) {
        return q.getFunctionName() + "(" + q.parametersToString() + ")";
    }
}
