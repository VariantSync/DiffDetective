package org.variantsync.diffdetective.variation.tree.view;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.util.ArrayList;
import java.util.List;

public class View {
    private static void inlineAt(final VariationTreeNode v, final Query q) {
        /// We assume that d is interesting for q.
        final List<VariationTreeNode> boringChildren = new ArrayList<>(v.getChildren().size());
        for (final VariationTreeNode child : v.getChildren()) {
            if (q.isInteresting(child)) {
                inlineAt(child, q);
            } else {
                boringChildren.add(child);
            }
        }

        for (final VariationTreeNode boringChild : boringChildren) {
            v.removeChild(boringChild);
        }
    }
    public static void inline(final VariationTree t, final Query q) {
        Assert.assertTrue(
                q.isInteresting(t.root())
        );

        inlineAt(t.root(), q);
    }
}
