package org.variantsync.diffdetective.variation.tree.view;

import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class View {
    private static void filterInline(final VariationTreeNode v, final Set<VariationTreeNode> view) {
        /// We assume that d is interesting for q.
        final List<VariationTreeNode> boringChildren = new ArrayList<>(v.getChildren().size());
        for (final VariationTreeNode child : v.getChildren()) {
            if (view.contains(child)) {
                filterInline(child, view);
            } else {
                boringChildren.add(child);
            }
        }

        for (final VariationTreeNode boringChild : boringChildren) {
            v.removeChild(boringChild);
        }
    }

    private static void addMeAndMyAncestorsTo(final VariationTreeNode n, Set<VariationTreeNode> nodes) {
        nodes.add(n);
        final VariationTreeNode p = n.getParent();
        if (p != null) {
            addMeAndMyAncestorsTo(p, nodes);
        }
    }

    public static void inline(final VariationTree t, final Query q) {
        final Set<VariationTreeNode> interestingNodes = new HashSet<>();

        t.forAllPreorder(node -> {
            if (q.isInteresting(node)) {
                addMeAndMyAncestorsTo(node, interestingNodes);
            }
        });

        filterInline(t.root(), interestingNodes);
    }
}
