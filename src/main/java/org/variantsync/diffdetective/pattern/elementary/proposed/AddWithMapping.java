package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

final class AddWithMapping extends ElementaryPattern {
    AddWithMapping() {
        super("AddWithMapping", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getAfterParent().isAdd();
    }
}
