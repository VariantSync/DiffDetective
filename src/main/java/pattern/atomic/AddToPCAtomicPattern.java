package pattern.atomic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Node;

import java.util.ArrayList;
import java.util.List;

public class AddToPCAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "AddToPC";

    public AddToPCAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch> getMatches(DiffNode codeNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if(codeNode.isAdd() && !codeNode.getAfterParent().isAdd()){
            Node fm = codeNode.getAfterParent().getAfterFeatureMapping();

            PatternMatch patternMatch = new PatternMatch(this,
                    codeNode.getFromLine(), codeNode.getToLine(), fm
            );
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
