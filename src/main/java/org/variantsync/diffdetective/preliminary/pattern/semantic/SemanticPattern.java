package org.variantsync.diffdetective.preliminary.pattern.semantic;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

import java.util.List;
import java.util.Optional;

@Deprecated
public abstract class SemanticPattern extends Pattern<DiffNode> implements FeatureContextReverseEngineering<DiffNode> {
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
    public Pattern<DiffNode> getPattern() {
        return this;
    }
}
