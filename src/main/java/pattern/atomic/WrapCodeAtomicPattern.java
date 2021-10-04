package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.Optional;

public class WrapCodeAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "WrapCode";

    public WrapCodeAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode codeNode) {
        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            if ((addAmount > 0 && remAmount == 0)
                    ||  (remAmount == 0 && addAmount == 0 && codeNode.getAfterDepth() > codeNode.getBeforeDepth())){
                final Lines diffLines = codeNode.getLinesInDiff();
                return Optional.of(new PatternMatch<>(this,
                        diffLines.getFromInclusive(), diffLines.getToExclusive()
                ));
            }
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
