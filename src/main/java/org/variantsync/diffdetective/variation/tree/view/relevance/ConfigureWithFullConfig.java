package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.tree.VariationNode;

/**
 * Relevance predicate that generates (partial) variants from variation trees.
 * This relevance predicate is the implementation of Equation 5 in our SPLC'23 paper.
 */
public class ConfigureWithFullConfig implements Relevance {
    private final Map<Object, Boolean> assignment;

    public ConfigureWithFullConfig(final Map<Object, Boolean> assignment) {
        this.assignment = assignment;
    }

    @Override
    public boolean test(VariationNode<?, ?> v) {
    	return v.getPresenceCondition().getValue(this.assignment);        
    }

    private <TreeNode extends VariationNode<TreeNode, ?>> void computeViewNodes(List<TreeNode> vs, Consumer<TreeNode> markRelevant) {
        for (final TreeNode c : vs) {
            computeViewNodes(c, markRelevant);
        }
    }

    @Override
    public <TreeNode extends VariationNode<TreeNode, ?>> void computeViewNodes(TreeNode v, Consumer<TreeNode> markRelevant) {
        // The implementation of this method must be semantically equivalent to the default implementation Relevance.super.computeViewNodes(v, markRelevant);
        // The implementation of this method is supposed to be optimized to avoid redundant SAT calls when possible.
        // This requirement is modelled explicitly in terms of the ConfigureSpec class.

        // If the child is an artifact, it has the same presence condition as v does, so it is also included in the view.
        // The root must be in any view to ensure consistency.
        if (v.isArtifact() || v.isRoot()) {
            markRelevant.accept(v);
            computeViewNodes(v.getChildren(), markRelevant);
        } else {
            computeViewNodesOfElifChain(v, markRelevant);
        }
    }

    /**
     * @return true if the given node was marked relevant
     */
    private <TreeNode extends VariationNode<TreeNode, ?>> boolean computeViewNodesOfElifChain(TreeNode v, Consumer<TreeNode> markRelevant) {
        Assert.assertTrue(v.isAnnotation());

        if (test(v)) {
            markRelevant.accept(v);
            computeViewNodes(v.getChildren(), markRelevant);
            return true;
        } else {
            // If a partial configuration excludes the node v (i.e, test(v) == false),
            // then any ELIF or ELSE branches might still be included.
            for (final TreeNode c : v.getChildren()) {
                NodeType ct = c.getNodeType();

                // We can skip all children that are not ELSE or ELIF nodes because these are excluded because v is exluded.
                // If our node v was deemed irrelevant and it has an ELSE node c, that ELSE node c must be relevant.
                if (ct == NodeType.ELSE) {
                    markRelevant.accept(v);
                    markRelevant.accept(c);
                    computeViewNodes(c.getChildren(), markRelevant);
                    return true;
                }

                // An ELIF might hold any formula which we have to test.
                // We handle ELIF nodes recursively because ELIFs might be nested.
                // If at least one branch of the ELIF chain is included, we must include also v in the view to retain consistency of the tree.
                if (ct == NodeType.ELIF && computeViewNodesOfElifChain(c, markRelevant)) {
                    markRelevant.accept(v);
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public String parametersToString() {
        return assignment.toString();
    }

    @Override
    public String getFunctionName() {
        return "configure";
    }

    @Override
    public String toString() {
        return Relevance.toString(this);
    }
}
