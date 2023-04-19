package org.variantsync.diffdetective.variation.tree.view.query;

import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.tree.VariationNode;

import java.util.function.Consumer;

public class VariantQuery implements Query {
    private final FixTrueFalse.Formula configuration;

    public VariantQuery(final FixTrueFalse.Formula configuration) {
        this.configuration = configuration;
    }

    public VariantQuery(final Node configuration) {
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
        return Query.toString(this);
    }
}
