package org.variantsync.diffdetective.preliminary.pattern.semantic;

import org.prop4j.Not;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.Optional;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;

@Deprecated
class AddIfdefWrapThen extends SemanticPattern {
    AddIfdefWrapThen() {
        super("AddIfdefWrapThen");
    }

    /*
    DETECTION:
        added if node
        has an unchanged code child
        has no elif children
        has an added else child
            which has an added code child
     */
    @Override
    public Optional<PatternMatch<DiffNode<?>>> match(DiffNode<?> annotationNode) {
        if(annotationNode.isAdd() && annotationNode.isIf()){
            boolean nonCodeInIf = false;
            DiffNode<?> elseNode = null;
            for(DiffNode<?> child : annotationNode.getAllChildren()){
                if(child.isElif()){
                    return Optional.empty();
                }
                if(child.isArtifact() && child.isNon()){
                    nonCodeInIf = true;
                }
                if(child.isElse() && child.isAdd()){
                    elseNode = child;
                }
            }

            if(elseNode == null || !nonCodeInIf){
                return Optional.empty();
            }

            boolean addedCodeInElse = false;
            for(DiffNode<?> child : elseNode.getAllChildren()){
                if(child.isArtifact() && child.isAdd()){
                    addedCodeInElse = true;
                }
            }

            if(!addedCodeInElse){
                return Optional.empty();
            }

            return Optional.of(new PatternMatch<>(this,
                    annotationNode.getLinesInDiff().fromInclusive(), elseNode.getLinesInDiff().toExclusive(),
                    annotationNode.getFeatureMapping(AFTER)
            ));
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode<?>> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(new Not(patternMatch.getFeatureMappings()[0]))
        };
    }
}
