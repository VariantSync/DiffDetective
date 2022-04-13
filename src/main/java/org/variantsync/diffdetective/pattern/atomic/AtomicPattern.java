package org.variantsync.diffdetective.pattern.atomic;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.EditPattern;

public abstract class AtomicPattern extends EditPattern<DiffNode> {
    private final DiffType diffType;

    /**
     * Each atomic pattern handles exactly one DiffType.
     */
    public AtomicPattern(final String name, final DiffType diffType) {
        super(name);
        this.diffType = diffType;
    }

    public DiffType getDiffType() {
        return diffType;
    }

    /**
     * @param codeNode Node which has code type CODE and whose DiffType is the same as this patterns DiffType.
     * @return True if given node matches this pattern.
     */
    protected abstract boolean matchesCodeNode(DiffNode codeNode);

    /**
     * @return True if this pattern matches the given node and node is code.
     */
    @Override
    public final boolean matches(DiffNode node) {
        return node.isCode() && node.diffType == diffType && matchesCodeNode(node);
    }

    public boolean anyMatch(final DiffTree t) {
        return t.anyMatch(this::matches);
    }
}
