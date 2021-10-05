package pattern;

import diff.difftree.DiffNode;

public abstract class SemanticPattern extends EditPattern<DiffNode> {
    public SemanticPattern(String name) {
        super(name);
    }
}
