package org.variantsync.diffdetective.variation.tree.view.relevance;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FixTrueFalse.Formula;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.view.relevance.spec.ConfigureSpec;

/**
 * Relevance predicate that generates (partial) variants from variation trees.
 * This relevance predicate is the implementation of Equation 5 in our SPLC'23 paper.
 */
public class Configure implements Relevance {
    private final Formula configuration;

    /**
     * Same as {@link Configure#Configure(Node)} but with a formula that is witnessed to
     * not contain true or false constants not at the root.
     * Workaround for FeatureIDE bug <a href="https://github.com/FeatureIDE/FeatureIDE/issues/1333">FeatureIDE Issue 1333</a>.
     */
    public Configure(final Formula configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a configuration relevance from a propositional formula that encodes selections
     * and deselections of variables.
     * Typically, the given formula should be in conjunctive normal form.
     * The given configuration may be partial or complete.
     * @param configuration A propositional formula that denotes selections and deselections.
     */
    public Configure(final Node configuration) {
        this(FixTrueFalse.EliminateTrueAndFalse(configuration));
    }

    /**
     * Create a configuration from an assignment of variable names to boolean values.
     * The given assignment may be complete or partial.
     * Internally, a big conjunction of literals is created:
     * <pre>
     *           ⋀ f              ∧          ⋀ ¬ f
     *   (f, true) ∈ assignment    (f, false) ∈ assignment
     * </pre>
     *
     * As an example, suppose the map contains the following entries:
     * <pre>
     *   A ↦ true
     *   B ↦ false
     *   C ↦ true
     * </pre>
     * then we construct a formula A ∧ (¬ B) ∧ C.
     */
    public Configure(final Map<String, Boolean> assignment) {
        // We use commutativity of ∧ to iterate the map only once instead of twice as shown in the formula above.
        final Formula[] fixedFeatures = new Formula[assignment.size()];
        int i = 0;
        for (Entry<String, Boolean> entry : assignment.entrySet()) {
            fixedFeatures[i] = Formula.var(entry.getKey());
            if (!entry.getValue()) {
                fixedFeatures[i] = Formula.not(fixedFeatures[i]);
            }

            ++i;
        }

        this.configuration = Formula.and(fixedFeatures);
    }

    @Override
    public boolean test(VariationNode<?, ?> v) {
        return ConfigureSpec.test(configuration, v);
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
        final NodeType vt = v.getNodeType();
        Assert.assertTrue(vt == NodeType.IF || vt == NodeType.ELSE || vt == NodeType.ELIF);

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
        return configuration.get().toString(NodeWriter.logicalSymbols);
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
