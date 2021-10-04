package pattern;

import analysis.data.PatternMatch;
import evaluation.FeatureContext;

import java.util.Optional;

public abstract class EditPattern<E> {
    protected String name;

    public EditPattern() {
        this.name = this.getClass().getSimpleName();
    }

    public EditPattern(final String name) {
        this.name = name;
    }

    public abstract Optional<PatternMatch<E>> match(E x);

    public abstract FeatureContext[] getFeatureContexts(PatternMatch<E> patternMatch);

    public String getName(){
        return this.name;
    }
}
