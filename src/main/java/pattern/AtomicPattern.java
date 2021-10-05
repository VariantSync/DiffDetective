package pattern;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;

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
}
