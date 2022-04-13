package preliminary.pattern.atomic;

import diff.Lines;
import diff.difftree.DiffNode;
import pattern.EditPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import preliminary.analysis.data.PatternMatch;
import preliminary.evaluation.FeatureContext;
import preliminary.pattern.FeatureContextReverseEngineering;

@Deprecated
public final class FeatureContextOfReconfiguration implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public EditPattern<DiffNode> getPattern() {
        return ProposedAtomicPatterns.Reconfiguration;
    }

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode codeNode) {
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
