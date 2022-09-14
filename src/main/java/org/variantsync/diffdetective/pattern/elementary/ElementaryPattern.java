package org.variantsync.diffdetective.pattern.elementary;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.EditPattern;

/**
 * Abstract class for elementary edit pattern according to our ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
public abstract class ElementaryPattern extends EditPattern<DiffNode> {
    private final DiffType diffType;

    /**
     * Each elementary pattern handles exactly one DiffType.
     * @param name unique identifier (see {@link EditPattern}).
     * @param diffType This pattern matches only {@link DiffNode}s of the given {@link DiffType}.
     */
    public ElementaryPattern(final String name, final DiffType diffType) {
        super(name);
        this.diffType = diffType;
    }

    /**
     * Returns the diff type nodes must have to be matched to this pattern.
     */
    public DiffType getDiffType() {
        return diffType;
    }

    /**
     * Returns true iff given node matches this pattern.
     * @param codeNode Node which has node type CODE and whose DiffType is the same as this patterns DiffType.
     */
    protected abstract boolean matchesCodeNode(DiffNode codeNode);

    /**
     * Returns true if this pattern matches the given node and node is code.
     */
    @Override
    public final boolean matches(DiffNode node) {
        return node.isCode() && node.diffType == diffType && matchesCodeNode(node);
    }

    /**
     * Returns true iff this pattern matches at leat one node on the given tree.
     */
    public boolean anyMatch(final DiffTree t) {
        return t.anyMatch(this::matches);
    }
}
