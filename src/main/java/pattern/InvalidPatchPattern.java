package pattern;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;

import java.util.List;

public class InvalidPatchPattern extends EditPattern {
    public static final String PATTERN_NAME = "InvalidPatch";

    public InvalidPatchPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch> getMatches(Object x) {
        return null;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return null;
    }
}
