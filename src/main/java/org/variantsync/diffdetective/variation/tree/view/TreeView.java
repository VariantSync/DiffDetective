package org.variantsync.diffdetective.variation.tree.view;

import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

/**
 * This class groups the implementations for functions that generate views on variation trees,
 * as described in Chapter 3 of our SPLC'23 paper - Views on Edits to Variational Software. 
 */
public final class TreeView {
    /**
     * Private constructor to prevent instantiation.
     */
    private TreeView() {}

    /**
     * Mutates a variation tree inplace to a view on itself.
     * Assuming that the given node v is the root of a variation tree,
     * v will be the root of a view on that variation tree after running this procedure.
     * @param v The root of the tree to turn into a view on itself.
     * @param inView A predicate that determines for each node whether it should be within the view or not.
     * @param <TreeNode> he type of the nodes within the given tree.
     */
    public static <TreeNode extends VariationNode<TreeNode>> void treeInline(final TreeNode v, final Predicate<TreeNode> inView) {
        final List<TreeNode> boringChildren = new ArrayList<>(v.getChildren().size());
        for (final TreeNode child : v.getChildren()) {
            if (!inView.test(child)) {
                boringChildren.add(child);
            }

            treeInline(child, inView);
        }

        for (final TreeNode boringChild : boringChildren) {
            v.removeChild(boringChild);
        }
    }

    /**
     * Mutates a variation tree inplace to a view on itself.
     * After running this procedure, the given variation tree t will represent a view.
     * @param t The variation tree to turn into a view.
     * @param r A relevance predicate that determines for each node in the tree whether it should be
     *          contained in the view or should be excluded. Will not be altered by running this procedure.
     */
    public static void treeInline(final VariationTree t, final Relevance r) {
        final Set<VariationTreeNode> interestingNodes = new HashSet<>();
        r.computeViewNodes(t.root(), interestingNodes::add);
        treeInline(t.root(), interestingNodes::contains);
    }

    /**
     * Creates a view on the given variation tree as described by the given relevance predicate.
     * This function is side-effect free.
     * Thre given variation tree and relevance will not be altered.
     * This function corresponds to Equation 4 in our SPLC'23 paper - Views on Edits to Variational Software.
     * @param t The variation tree to generate a view on.
     * @param r A relevance predicate that determines for each node in the
     *          tree whether it should be contained in the view or should be excluded.
     * @return A variation tree that represents a view on the given variation tree t.
     */
    public static VariationTree tree(final VariationTree t, final Relevance r) {
        final VariationTree copy = t.deepCopy();
        treeInline(copy, r);
        return copy;
    }
}
