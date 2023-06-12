package org.variantsync.diffdetective.variation.tree.view.relevance;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.tree.VariationNode;

import java.util.function.Consumer;

/**
 * Relevance predicate that generates (partial) variants from variation trees.
 * This relevance predicate is the implementation of Equation 5 in our SPLC'23 paper.
 */
public class Configure implements Relevance {
    private final FixTrueFalse.Formula configuration;

    /**
     * Same as {@link Configure#Configure(Node)} but with a formula that is witnessed to
     * not contain true or false constants not at the root.
     * Workaround for FeatureIDE bug <a href="https://github.com/FeatureIDE/FeatureIDE/issues/1333">FeatureIDE Issue 1333</a>.
     */
    public Configure(final FixTrueFalse.Formula configuration) {
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

    @Override
    public boolean test(VariationNode<?> v) {
        return SAT.isSatisfiable(
                FixTrueFalse.Formula.and(
                        configuration,
                        FixTrueFalse.EliminateTrueAndFalse(v.getPresenceCondition())
                )
        );
    }

    @Override
    public <TreeNode extends VariationNode<TreeNode>> void computeViewNodes(TreeNode v, Consumer<TreeNode> markRelevant) {
        markRelevant.accept(v);

        for (final TreeNode c : v.getChildren()) {
            // If the child is an artifact it has the same presence condition as we do, so it is also included in the view.
            if (c.isArtifact() || test(c)) {
                computeViewNodes(c, markRelevant);
            }
        }
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
