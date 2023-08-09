package org.variantsync.diffdetective.variation.tree;

import java.util.ArrayList;
import java.util.List;

import org.prop4j.And;
import org.prop4j.Node;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.VariationLabel;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public final class VariationTreeNode {
    private VariationTreeNode() {
    }

    /**
     * Returns the first {@code if} node in the path from this node upwards to the root.
     */
    public static <L extends Label, N extends TreeNode<N, VariationLabel<L>>> N getIfNode(N node) {
        if (node.getLabel().isIf()) {
            return node.upCast();
        }
        return getIfNode(node.getParent());
    }

    /**
     * Same as {@link getFeatureMapping} but returns a list of formulas representing a conjunction.
     */
    private static <L extends Label, N extends TreeNode<N, VariationLabel<L>>> List<Node> getFeatureMappingClauses(N node) {
        final var parent = node.getParent();

        if (node.getLabel().isElse() || node.getLabel().isElif()) {
            List<Node> and = new ArrayList<>();

            if (node.getLabel().isElif()) {
                and.add(node.getLabel().getFormula());
            }

            // Negate all previous cases
            var ancestor = parent;
            while (!ancestor.getLabel().isIf()) {
                if (ancestor.getLabel().isElif()) {
                    and.add(negate(ancestor.getLabel().getFormula()));
                } else {
                    throw new RuntimeException("Expected If or Elif above Else or Elif but got " + ancestor.getLabel().getNodeType() + " from " + ancestor);
                    // Assert.assertTrue(ancestor.isArtifact());
                }
                ancestor = ancestor.getParent();
            }
            and.add(negate(ancestor.getLabel().getFormula()));

            return and;
        } else if (node.getLabel().isArtifact()) {
            return getFeatureMappingClauses(parent);
        }

        return List.of(node.getLabel().getFormula());
    }

    /**
     * Returns the full feature mapping formula of this node.
     *
     * <p>The feature mapping of an {@link NodeType#IF} node is its {@link #getFormula()
     * direct feature mapping}. The feature mapping of {@link NodeType#ELSE} and {@link
     * NodeType#ELIF} nodes is determined by all formulas in the respective if-elif-else chain. The
     * feature mapping of an {@link NodeType#ARTIFACT artifact} node is the feature mapping of its
     * parent. See Equation (1) in
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     *
     * @return the feature mapping of this node
     */
    public static <L extends Label, N extends TreeNode<N, VariationLabel<L>>> Node getFeatureMapping(N node) {
        final List<Node> fmClauses = getFeatureMappingClauses(node);
        if (fmClauses.size() == 1) {
            return fmClauses.get(0);
        }
        return new And(fmClauses);
    }

    /**
     * Returns the presence condition clauses of this node.
     *
     * @return a list representing a conjunction (i.e., all clauses should be combined with boolean
     * AND)
     * @see getPresenceCondition
     */
    private static <L extends Label, N extends TreeNode<N, VariationLabel<L>>> List<Node> getPresenceConditionClauses(N node) {
        final var parent = node.getParent();

        if (node.getLabel().isElse() || node.getLabel().isElif()) {
            final List<Node> clauses = new ArrayList<>(getFeatureMappingClauses(node));

            // Find corresponding if
            var correspondingIf = parent;
            while (!correspondingIf.getLabel().isIf()) {
                correspondingIf = correspondingIf.getParent();
            }

            // If this elif-else-chain was again nested in another annotation, add its pc.
            final var outerNesting = correspondingIf.getParent();
            if (outerNesting != null) {
                clauses.addAll(getPresenceConditionClauses(outerNesting));
            }

            return clauses;
        } else if (node.getLabel().isArtifact()) {
            return getPresenceConditionClauses(parent);
        }

        // this is mapping or root
        final List<Node> clauses;
        if (parent == null) {
            clauses = new ArrayList<>(1);
        } else {
            clauses = getPresenceConditionClauses(parent);
        }
        clauses.add(node.getLabel().getFormula());
        return clauses;
    }

    /**
     * Returns the presence condition of this node.
     * See Equation (2) in
     * <a href="https://github.com/SoftVarE-Group/Papers/raw/main/2022/2022-ESECFSE-Bittner.pdf">
     * our paper</a>.
     */
    public static <L extends Label, N extends TreeNode<N, VariationLabel<L>>> Node getPresenceCondition(N node) {
        final List<Node> pcClauses = getPresenceConditionClauses(node);
        if (pcClauses.size() == 1) {
            return pcClauses.get(0);
        }
        return new And(pcClauses);
    }
}
