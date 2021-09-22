package pattern.semantic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class AddIfdefWrapElseSemanticPattern extends SemanticPattern{

    public static final String PATTERN_NAME = "AddIfdefWrapElseSEM";

    public AddIfdefWrapElseSemanticPattern() {
        this.name = PATTERN_NAME;
    }

    /*
    DETECTION:
        added if node
        has an added code child
        has no elif children
        has an added else child
            which has an unchanged code child
     */
    @Override
    public List<PatternMatch<DiffNode>> getMatches(DiffNode annotationNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if(annotationNode.isAdd() && annotationNode.isIf()){

            boolean addedCodeInIf = false;
            DiffNode elseNode = null;
            for(DiffNode child : annotationNode.getAllChildren()){
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

            boolean noneCodeInElse = false;
            for(DiffNode child : elseNode.getAllChildren()){
                if(child.isCode() && child.isNon()){
                    noneCodeInElse = true;
                }
            }

            if(!noneCodeInElse){
                return patternMatches;
            }

            PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                    annotationNode.getLinesInDiff().getFromInclusive(), elseNode.getLinesInDiff().getToExclusive(),
                    annotationNode.getAfterFeatureMapping()
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
