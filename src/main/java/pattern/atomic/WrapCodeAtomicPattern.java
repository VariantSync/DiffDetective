package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import evaluation.FeatureContext;
import pattern.AtomicPattern;

public class WrapCodeAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "WrapCode";

    public WrapCodeAtomicPattern() {
        super(PATTERN_NAME, DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        int addAmount = codeNode.getAddAmount();
        int remAmount = codeNode.getRemAmount();
        return (addAmount > 0 && remAmount == 0)
                ||  (remAmount == 0 && addAmount == 0 && codeNode.getAfterDepth() > codeNode.getBeforeDepth());
    }

    @Override
    public PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode) {
        final Lines diffLines = codeNode.getLinesInDiff();
        return new PatternMatch<>(this,
                diffLines.getFromInclusive(), diffLines.getToExclusive()
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
