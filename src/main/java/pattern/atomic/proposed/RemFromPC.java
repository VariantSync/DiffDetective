package pattern.atomic.proposed;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.atomic.AtomicPattern;

final class RemFromPC extends AtomicPattern {
    RemFromPC() {
        super("RemFromPC", DiffType.REM);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return !codeNode.getBeforeParent().isRem();
    }

    @Override
    public PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode) {
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
