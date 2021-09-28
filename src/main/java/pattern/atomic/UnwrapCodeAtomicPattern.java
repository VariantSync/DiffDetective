package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class UnwrapCodeAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "UnwrapCode";

    public UnwrapCodeAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch<DiffNode>> getMatches(DiffNode codeNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            if ((remAmount > 0 && addAmount == 0)
                    ||  (remAmount == 0 && addAmount == 0 && codeNode.getBeforeDepth() > codeNode.getAfterDepth())){
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
