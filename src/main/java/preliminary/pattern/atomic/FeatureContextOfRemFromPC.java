package preliminary.pattern.atomic;

import diff.Lines;
import diff.difftree.DiffNode;
import org.prop4j.Node;
import pattern.EditPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import preliminary.analysis.data.PatternMatch;
import preliminary.evaluation.FeatureContext;
import preliminary.pattern.FeatureContextReverseEngineering;

@Deprecated
public final class FeatureContextOfRemFromPC implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public EditPattern<DiffNode> getPattern() {
        return ProposedAtomicPatterns.RemFromPC;
    }

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode codeNode) {
        final Node fm = codeNode.getBeforeParent().getBeforeFeatureMapping();
        final Lines diffLines = codeNode.getLinesInDiff();

        return new PatternMatch<>(this,
                diffLines.getFromInclusive(), diffLines.getToExclusive(), fm
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0], true)
        };
    }
}
