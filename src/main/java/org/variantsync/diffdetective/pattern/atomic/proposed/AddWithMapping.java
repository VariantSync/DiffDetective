package org.variantsync.diffdetective.pattern.atomic.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;

final class AddWithMapping extends AtomicPattern {
    AddWithMapping() {
        super("AddWithMapping", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getAfterParent().isAdd();
    }
}
