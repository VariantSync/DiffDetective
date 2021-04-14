package pattern;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;

import java.util.List;

public abstract class EditPattern<E> {
    protected String name;

    public EditPattern() {
        this.name = this.getClass().getSimpleName();
    }

    public abstract List<PatternMatch> getMatches(E x);

    public abstract FeatureContext[] getFeatureContexts(PatternMatch patternMatch);

    public String getName(){
        return this.name;
    }
}
