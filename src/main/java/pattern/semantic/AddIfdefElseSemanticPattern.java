package pattern.semantic;

import analysis.data.PatternMatch;
import diff.data.DiffNode;
import evaluation.FeatureContext;
import org.prop4j.Not;

import java.util.ArrayList;
import java.util.List;

public class AddIfdefElseSemanticPattern extends SemanticPattern{

    public static final String PATTERN_NAME = "AddIfdefElseSEM";

    public AddIfdefElseSemanticPattern() {
        this.name = PATTERN_NAME;
    }

    /*
    DETECTION:
        added if node
        has an added code child
        has no elif children
        has an added else child
          which has an added code child
     */
    @Override
    public List<PatternMatch> getMatches(DiffNode annotationNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if(annotationNode.isAdd() && annotationNode.isIf()){

            boolean addedCodeInIf = false;
            DiffNode elseNode = null;
            for(DiffNode child : annotationNode.getChildren()){
                if(child.isElif()){
                    return patternMatches;
                }
                if(child.isCode() && child.isAdd()){
                    addedCodeInIf = true;
                }
                if(child.isElse() && child.isAdd()){
                    elseNode = child;
                }
            }

            if(elseNode == null || !addedCodeInIf){
                return patternMatches;
            }

            boolean addedCodeInElse = false;
            for(DiffNode child : elseNode.getChildren()) {
                if(child.isCode() && child.isAdd()){
                    addedCodeInElse = true;
                }
            }

            if(!addedCodeInElse){
                return patternMatches;
            }

            PatternMatch patternMatch = new PatternMatch(this,
                    annotationNode.getFromLine(), elseNode.getToLine(),
                    annotationNode.getAfterFeatureMapping()
            );
            patternMatches.add(patternMatch);
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0]),
                new FeatureContext(new Not(patternMatch.getFeatureMappings()[0]))
        };
    }
}
