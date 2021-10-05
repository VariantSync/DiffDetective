package pattern;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import org.eclipse.jgit.annotations.NonNull;

import java.util.Optional;

public abstract class AtomicPattern extends EditPattern<DiffNode> {
    public AtomicPattern(String name) {
        super(name);
    }

    protected abstract boolean matchesCodeNode(DiffNode codeNode);
    protected abstract PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode);

    public final boolean matches(DiffNode node) {
        return node.isCode() && matchesCodeNode(node);
    }

    @Override
    public final Optional<PatternMatch<DiffNode>> match(DiffNode x) {
        if (matches(x)) {
            return Optional.of(createMatchOnCodeNode(x));
        }

        return Optional.empty();
    }

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
