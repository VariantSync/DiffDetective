package pattern.atomic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class WrapCodeAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "WrapCode";

    public WrapCodeAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch> getMatches(DiffNode codeNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            if ((addAmount > 0 && remAmount == 0)
                    ||  (remAmount == 0 && addAmount == 0 && codeNode.getAfterDepth() > codeNode.getBeforeDepth())){
                PatternMatch patternMatch = new PatternMatch(this,
                        codeNode.getFromLine(), codeNode.getToLine()
                );
                patternMatches.add(patternMatch);
            }
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
