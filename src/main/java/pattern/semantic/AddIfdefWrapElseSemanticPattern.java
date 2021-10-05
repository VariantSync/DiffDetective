package pattern.semantic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import pattern.SemanticPattern;

import java.util.Optional;

public class AddIfdefWrapElseSemanticPattern extends SemanticPattern {
    public static final String PATTERN_NAME = "AddIfdefWrapElseSEM";

    public AddIfdefWrapElseSemanticPattern() {
        super(PATTERN_NAME);
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
    public Optional<PatternMatch<DiffNode>> match(DiffNode annotationNode) {
        if(annotationNode.isAdd() && annotationNode.isIf()){
            boolean addedCodeInIf = false;
            DiffNode elseNode = null;
            for(DiffNode child : annotationNode.getAllChildren()){
                if(child.isElif()){
                    return Optional.empty();
                }
                if(child.isCode() && child.isAdd()){
                    addedCodeInIf = true;
                }
                if(child.isElse() && child.isAdd()){
                    elseNode = child;
                }
            }

            if(elseNode == null || !addedCodeInIf){
                return Optional.empty();
            }

            boolean noneCodeInElse = false;
            for(DiffNode child : elseNode.getAllChildren()){
                if(child.isCode() && child.isNon()){
                    noneCodeInElse = true;
                }
            }

            if(!noneCodeInElse){
                return Optional.empty();
            }

            return Optional.of(new PatternMatch<>(this,
                    annotationNode.getLinesInDiff().getFromInclusive(), elseNode.getLinesInDiff().getToExclusive(),
                    annotationNode.getAfterFeatureMapping()
            ));
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
