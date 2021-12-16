package pattern.semantic;

import diff.difftree.DiffNode;
import pattern.EditPattern;

import java.util.List;

public abstract class SemanticPattern extends EditPattern<DiffNode> {
    public SemanticPattern(String name) {
        super(name);
    }

    public static final SemanticPattern AddIfdefElif = new AddIfdefElif();
    public static final SemanticPattern AddIfdefElse = new AddIfdefElse();
    public static final SemanticPattern AddIfdefWrapElse = new AddIfdefWrapElse();
    public static final SemanticPattern AddIfdefWrapThen = new AddIfdefWrapThen();
    public static final SemanticPattern MoveElse = new MoveElse();

    public static final List<SemanticPattern> All = List.of(
            AddIfdefElif, AddIfdefElse, AddIfdefWrapElse, AddIfdefWrapThen, MoveElse
    );
}
