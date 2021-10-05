package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.AtomicPattern;

public class RemWithMapping extends AtomicPattern {
    public static final String PATTERN_NAME = "RemWithMapping";

    public RemWithMapping() {
        super(PATTERN_NAME);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.isRem() && codeNode.getBeforeParent().isRem();
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
                new FeatureContext(null),
                new FeatureContext(patternMatch.getFeatureMappings()[0], true)
        };
    }
}
