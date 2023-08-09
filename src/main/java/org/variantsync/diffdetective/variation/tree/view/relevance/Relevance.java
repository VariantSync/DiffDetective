package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.tree.TreeNode;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Abstract definition of relevance predicates as defined in Section 3 in our SPLC'23 paper - Views on Edits to Variational Software.
 * A relevance predicate decides which nodes within a variation tree are interesting for,
 * and hence should be included in, a view on that variation tree.
 * Thus, a relevance predicate is a predicate on nodes in a variation trees.
 * Moreover, this interface provides methods to access a predicates metadata for debugging
 * and (de-)serialization.
 */
public interface Relevance extends Predicate<TreeNode<?, VariationLabel<?>>> {
    /**
     * @return The name of this relevance predicate's type.
     */
    String getFunctionName();

    /**
     * @return The parameters set for this particular relevance predicate, as a comma-separated string (without braces).
     */
    String parametersToString();

    /**
     * Delegates to {@link Relevance#computeViewNodesCheckAll(Relevance, VariationNode, Consumer)} with this relevance
     * as the first parameter.
     */
    default <T extends TreeNode<T, VariationLabel<?>>> void computeViewNodes(final T v, final Consumer<T> markRelevant) {
        computeViewNodesCheckAll(this, v, markRelevant);
    }

    /**
     * Marks all nodes that should be contained within a view on the given tree.
     * In particular, this function checks each node in the given tree v on relevance.
     * For each node that is deemed relevant by the given relevance predicate rho, that node and all its ancestors are
     * marked as relevant by invoking the given callback markRelevant.
     * This function tests the relevance predicate on all nodes separately and performs no optimizations.
     * @param rho The relevance predicate to test on all nodes.
     * @param v The root node the tree to test for relevance.
     * @param markRelevant Callback that is invoked on each tree node that is deemed relevant.
     * @param <TreeNode> The type of the nodes within the given tree.
     */
    static <T extends TreeNode<T, VariationLabel<?>>> void computeViewNodesCheckAll(final Relevance rho, final T v, final Consumer<T> markRelevant) {
        for (final T c : v.getChildren()) {
            if (rho.test(c)) {
                c.forMeAndMyAncestors(markRelevant);
            }

            computeViewNodesCheckAll(rho, c, markRelevant);
        }
    }

    /**
     * Default implementation for {@link Object#toString()} that can be reused by implementing classes.
     * The produced string will look like a function call.
     * @param relevance The relevance predicate to turn into a string.
     * @return {@link Relevance#getFunctionName()} + "(" + {@link Relevance#parametersToString()} + ")"
     */
    static String toString(Relevance relevance) {
        return relevance.getFunctionName() + "(" + relevance.parametersToString() + ")";
    }
}
