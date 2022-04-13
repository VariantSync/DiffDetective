package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

final class RemWithMapping extends ElementaryPattern {
    RemWithMapping() {
        super("RemWithMapping", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getBeforeParent().isRem();
    }
}
