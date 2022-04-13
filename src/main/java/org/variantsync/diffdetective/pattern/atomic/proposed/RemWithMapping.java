package org.variantsync.diffdetective.pattern.atomic.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;

final class RemWithMapping extends AtomicPattern {
    RemWithMapping() {
        super("RemWithMapping", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getBeforeParent().isRem();
    }
}
