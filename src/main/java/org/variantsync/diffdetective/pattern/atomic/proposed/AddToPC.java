package org.variantsync.diffdetective.pattern.atomic.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;

final class AddToPC extends AtomicPattern {
    AddToPC() {
        super("AddToPC", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode node) {
        return !node.getAfterParent().isAdd();
    }
}
