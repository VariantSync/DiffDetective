package pattern.atomic;

import analysis.data.PatternMatch;
import diff.data.DiffNode;
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
    public List<PatternMatch> getMatches(DiffNode codeNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if (codeNode.isAdd() && codeNode.getAfterParent().isAdd()) {
            Node fm = codeNode.getAfterParent().getAfterFeatureMapping();

            PatternMatch patternMatch = new PatternMatch(this,
                    codeNode.getFromLine(),
                    codeNode.getToLine(), fm);
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        if(patternMatch.getFeatureMappings() == null){
            System.out.println();
        }
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
