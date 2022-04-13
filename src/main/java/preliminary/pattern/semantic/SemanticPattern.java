package preliminary.pattern.semantic;

import diff.difftree.DiffNode;
import pattern.EditPattern;
import preliminary.analysis.data.PatternMatch;
import preliminary.pattern.FeatureContextReverseEngineering;

import java.util.List;
import java.util.Optional;

@Deprecated
public abstract class SemanticPattern extends EditPattern<DiffNode> implements FeatureContextReverseEngineering<DiffNode> {
    public static final SemanticPattern AddIfdefElif = new AddIfdefElif();
    public static final SemanticPattern AddIfdefElse = new AddIfdefElse();
    public static final SemanticPattern AddIfdefWrapElse = new AddIfdefWrapElse();
    public static final SemanticPattern AddIfdefWrapThen = new AddIfdefWrapThen();
    public static final SemanticPattern MoveElse = new MoveElse();

    public static final List<SemanticPattern> All = List.of(
            AddIfdefElif, AddIfdefElse, AddIfdefWrapElse, AddIfdefWrapThen, MoveElse
    );

    public SemanticPattern(String name) {
        super(name);
    }

    @Override
    public boolean matches(DiffNode diffNode) {
        return match(diffNode).isPresent();
    }

    public abstract Optional<PatternMatch<DiffNode>> match(DiffNode annotationNode);

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode diffNode) {
        return match(diffNode).orElseThrow();
    }

    @Override
    public EditPattern<DiffNode> getPattern() {
        return this;
    }
}
