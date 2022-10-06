package org.variantsync.diffdetective.preliminary.pattern.semantic;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;

import java.util.Optional;

@Deprecated
class AddIfdefWrapElse extends SemanticPattern {
    public AddIfdefWrapElse() {
        super("AddIfdefWrapElse");
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
                if(child.isArtifact() && child.isAdd()){
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
                if(child.isArtifact() && child.isNon()){
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
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
