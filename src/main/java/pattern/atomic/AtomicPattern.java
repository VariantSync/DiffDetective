package pattern.atomic;

import diff.difftree.DiffNode;
import pattern.EditPattern;

public abstract class AtomicPattern extends EditPattern<DiffNode> {

    public AtomicPattern() {
        super();
    }

    public AtomicPattern(final String name) {
        super(name);
    }
}
