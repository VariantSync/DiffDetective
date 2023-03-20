package org.variantsync.diffdetective.variation.diff.view;

import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

public class DiffView {
    public static DiffTree diff(final DiffTree d, final Query q) {
        // treeify
        final BadVDiff badDiff = BadVDiff.fromGood(d);

        // create view
        TreeView.treeInline(badDiff.diff(), q);

        // unify
        final DiffTree goodDiff = badDiff.toGood();
        goodDiff.assertConsistency();
        return goodDiff;
    }
}
