package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import evaluation.FeatureContext;
import org.prop4j.Node;
import pattern.AtomicPattern;

public class AddWithMappingAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "AddWithMapping";

    public AddWithMappingAtomicPattern() {
        super(PATTERN_NAME, DiffType.ADD);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        return codeNode.getAfterParent().isAdd();
    }

    @Override
    public PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode) {
        final Node fm = codeNode.getAfterParent().getAfterFeatureMapping();
        final Lines diffLines = codeNode.getLinesInDiff();

        return new PatternMatch<>(this,
                diffLines.getFromInclusive(),
                diffLines.getToExclusive(), fm
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        if(patternMatch.getFeatureMappings() == null){
            System.out.println();
        }
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
