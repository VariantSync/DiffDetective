package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.Optional;

public class UnwrapCodeAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "UnwrapCode";

    public UnwrapCodeAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode codeNode) {
        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            if ((remAmount > 0 && addAmount == 0)
                    ||  (remAmount == 0 && addAmount == 0 && codeNode.getBeforeDepth() > codeNode.getAfterDepth())){
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
