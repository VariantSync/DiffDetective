package org.variantsync.diffdetective.pattern.atomic.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;

final class RemFromPC extends AtomicPattern {
    RemFromPC() {
        super("RemFromPC", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return !codeNode.getBeforeParent().isRem();
    }
}
