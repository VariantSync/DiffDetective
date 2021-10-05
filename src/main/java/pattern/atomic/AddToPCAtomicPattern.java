package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.AtomicPattern;

public class AddToPCAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "AddToPC";

    public AddToPCAtomicPattern() {
        super(PATTERN_NAME);
    }

    @Override
    public boolean matchesCodeNode(DiffNode node) {
        return node.isAdd() && !node.getAfterParent().isAdd();
    }

    @Override
    public PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode) {
        final Node fm = codeNode.getAfterParent().getAfterFeatureMapping();
        final Lines diffLines = codeNode.getLinesInDiff();

        return new PatternMatch<>(this,
                diffLines.getFromInclusive(), diffLines.getToExclusive(), fm
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
