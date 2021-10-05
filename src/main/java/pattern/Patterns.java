package pattern;

import pattern.atomic.*;
import pattern.semantic.*;

public final class Patterns {
    private Patterns() {};

    public static final AtomicPattern[] ATOMIC = new AtomicPattern[]{
            new AddWithMappingAtomicPattern(),
            new RemWithMapping(),
            new AddToPCAtomicPattern(),
            new RemFromPCAtomicPattern(),
            new WrapCodeAtomicPattern(),
            new UnwrapCodeAtomicPattern(),
            new ChangePCAtomicPattern(),
    };

    public static final SemanticPattern[] SEMANTIC = new SemanticPattern[]{
            new AddIfdefElseSemanticPattern(),
            new AddIfdefElifSemanticPattern(),
            new AddIfdefWrapElseSemanticPattern(),
            new AddIfdefWrapThenSemanticPattern(),
            new MoveElseSemanticPattern(),
    };
}
