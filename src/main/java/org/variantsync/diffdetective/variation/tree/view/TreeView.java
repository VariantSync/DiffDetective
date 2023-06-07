package org.variantsync.diffdetective.variation.tree.view;

import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public class TreeView {
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

    public static void treeInline(final VariationTree t, final Query q) {
        final Set<VariationTreeNode> interestingNodes = new HashSet<>();
        q.computeViewNodes(t.root(), interestingNodes::add);
        treeInline(t.root(), interestingNodes::contains);
    }

    public static VariationTree tree(final VariationTree T, final Query q) {
        final VariationTree copy = T.deepCopy();
        treeInline(copy, q);
        return copy;
    }
}