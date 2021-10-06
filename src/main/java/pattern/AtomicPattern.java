package pattern;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import org.eclipse.jgit.annotations.NonNull;

import java.util.Optional;

public abstract class AtomicPattern extends EditPattern<DiffNode> {
    public AtomicPattern(String name) {
        super(name);
    }

    /**
     * @param codeNode Node of code type CODE to check for a pattern match.
     * @return True if given node matches this pattern.
     */
    protected abstract boolean matchesCodeNode(DiffNode codeNode);

    /**
     * Creates a PatternMatch object for the given codeNode.
     * Assumes matchesCodeNode(codeNode) == true.
     * @param codeNode A node that was matched to this pattern.
     * @return A PatternMatch object containing metadata when matching this pattern to the given node.
     */
    protected abstract PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode);

    /**
     * @return True if this pattern matches the given node and node is code.
     */
    public final boolean matches(DiffNode node) {
        return node.isCode() && matchesCodeNode(node);
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

    /**
     * Returns the atomic pattern that matches the given node.
     * Each node matches exactly one pattern.
     * @param node The node of which to find its atomic pattern.
     * @return Returns the atomic pattern that matches the given node.
     */
    public static @NonNull AtomicPattern getPattern(DiffNode node) {
        if (!node.isCode()) {
            throw new IllegalArgumentException("Expected a code node but got " + node.codeType + "!");
        }

        AtomicPattern match = null;
        for (final AtomicPattern p : Patterns.ATOMIC) {
            if (p.matchesCodeNode(node)) {
                if (match != null) {
                    throw new RuntimeException("BUG: Error in atomic pattern definition!\n"
                                    + "Node " + node + " matched " + match + " and " + p + "!");
                }
                match = p;
            }
        }

        if (match == null) {
            throw new RuntimeException("BUG: Error in atomic pattern definition!\n"
                    + "Node " + node + " did not match any pattern!");
        }

        return match;
    }
}
