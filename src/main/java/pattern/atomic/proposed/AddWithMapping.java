package pattern.atomic.proposed;

import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import pattern.atomic.AtomicPattern;

final class AddWithMapping extends AtomicPattern {
    AddWithMapping() {
        super("AddWithMapping", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getAfterParent().isAdd();
    }
}
