package pattern;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;

import java.util.List;

public class InvalidPatchPattern<E> extends EditPattern<E> {
    public static final String PATTERN_NAME = "InvalidPatch";

    public InvalidPatchPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch<E>> getMatches(E x) {
        return null;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<E> patternMatch) {
        return null;
    }
}
