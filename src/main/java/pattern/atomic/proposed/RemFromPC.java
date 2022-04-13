package pattern.atomic.proposed;

import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import pattern.atomic.AtomicPattern;

final class RemFromPC extends AtomicPattern {
    RemFromPC() {
        super("RemFromPC", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return !codeNode.getBeforeParent().isRem();
    }
}
