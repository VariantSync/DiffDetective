package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

public class AddWithMappingAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "AddWithMapping";

    public AddWithMappingAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch<DiffNode>> getMatches(DiffNode codeNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if (codeNode.isAdd() && codeNode.getAfterParent().isAdd()) {
            final Node fm = codeNode.getAfterParent().getAfterFeatureMapping();
            final Lines diffLines = codeNode.getLinesInDiff();

            PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                    diffLines.getFromInclusive(),
                    diffLines.getToExclusive(), fm);
            patternMatches.add(patternMatch);
        }
        return patternMatches;
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
