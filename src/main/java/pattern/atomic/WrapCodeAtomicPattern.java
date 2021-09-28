package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
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
    public List<PatternMatch<DiffNode>> getMatches(DiffNode codeNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            if ((addAmount > 0 && remAmount == 0)
                    ||  (remAmount == 0 && addAmount == 0 && codeNode.getAfterDepth() > codeNode.getBeforeDepth())){
                final Lines diffLines = codeNode.getLinesInDiff();
                PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                        diffLines.getFromInclusive(), diffLines.getToExclusive()
                );
                patternMatches.add(patternMatch);
            }
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
