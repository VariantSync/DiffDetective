package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

public class RemWithMapping extends AtomicPattern {
    public static final String PATTERN_NAME = "RemWithMapping";

    public RemWithMapping() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch<DiffNode>> getMatches(DiffNode codeNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if (codeNode.isRem() && codeNode.getBeforeParent().isRem()) {
            final Node fm = codeNode.getBeforeParent().getBeforeFeatureMapping();
            final Lines diffLines = codeNode.getLinesInDiff();

            PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                    diffLines.getFromInclusive(), diffLines.getToExclusive(), fm
            );
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(null),
                new FeatureContext(patternMatch.getFeatureMappings()[0], true)
        };
    }
}
