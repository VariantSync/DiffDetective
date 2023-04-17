package org.variantsync.diffdetective.variation.tree.view.query;

import org.prop4j.And;
import org.prop4j.Node;
import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.variation.tree.VariationNode;

import java.util.function.Consumer;

public class VariantQuery implements Query {
    private final Node configuration;

    public VariantQuery(final Node configuration) {
        this.configuration = configuration;
    }

    public static VariantQuery fromConfiguration(final Node configuration) {
        return new VariantQuery(FixTrueFalse.EliminateTrueAndFalse(configuration));
    }

    public static VariantQuery fromConfigurationWithoutTrueAndFalseLiterals(final Node configuration) {
        return new VariantQuery(configuration);
    }

    @Override
    public boolean test(VariationNode<?> v) {
        return SAT.isSatisfiableAlreadyEliminatedTrueAndFalse(
                new And(
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
