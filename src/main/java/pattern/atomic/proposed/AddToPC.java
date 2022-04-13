package pattern.atomic.proposed;

import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import pattern.atomic.AtomicPattern;

final class AddToPC extends AtomicPattern {
    AddToPC() {
        super("AddToPC", DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode node) {
        return !node.getAfterParent().isAdd();
    }
}
