package pattern.atomic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffType;
import pattern.EditPattern;

import java.util.Optional;

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
     * Creates a PatternMatch object for the given codeNode.
     * Assumes {@code matches(codeNode) == true}.
     * @param codeNode A node that was matched to this pattern.
     * @return A PatternMatch object containing metadata when matching this pattern to the given node.
     */
    protected abstract PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode);

    /**
     * @return True if this pattern matches the given node and node is code.
     */
    public final boolean matches(DiffNode node) {
        return node.isCode() && node.diffType == diffType && matchesCodeNode(node);
    }

    /**
     * Matches this pattern onto the given node.
     * @param x The node to match this pattern on.
     * @return A {@link PatternMatch<DiffNode>} if the given node matches this pattern (i.e., {@code matches(x) == true}). Empty otherwise.
     */
    @Override
    public final Optional<PatternMatch<DiffNode>> match(DiffNode x) {
        if (matches(x)) {
            return Optional.of(createMatchOnCodeNode(x));
        }

        return Optional.empty();
    }

    public boolean anyMatch(final DiffTree t) {
        return t.anyMatch(this::matches);
    }
}
