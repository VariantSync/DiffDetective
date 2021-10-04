package pattern;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;

import java.util.Optional;

public class InvalidPatchPattern<E> extends EditPattern<E> {
    public static final String PATTERN_NAME = "InvalidPatch";

    public InvalidPatchPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public Optional<PatternMatch<E>> match(E x) {
        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<E> patternMatch) {
        return null;
    }
}
