package org.variantsync.diffdetective.preliminary.pattern.semantic;

import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.Collection;
import java.util.Optional;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;

@Deprecated
class MoveElse extends SemanticPattern {
    MoveElse() {
        super("MoveElse");
    }

    /*
    DETECTION:
        added else node
        the parent of this node has a removed else child
        the parent has a child that is either also a child of the added or the removed else node
     */
    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode annotationNode) {
        if(annotationNode.isAdd() && annotationNode.isElse()){

            DiffNode removedElse = null;
            for(DiffNode parentsChild : annotationNode.getParent(AFTER).getAllChildren()){
                if(parentsChild.isElse() && parentsChild.isRem()){
                    removedElse = parentsChild;
                    break;
                }
            }

            if(removedElse == null){
                return Optional.empty();
            }

            Collection<DiffNode> commonAddElse = annotationNode.getAllChildren();
            commonAddElse.retainAll(annotationNode.getParent(AFTER).getAllChildren());

            Collection<DiffNode> commonRemElse = removedElse.getAllChildren();
            commonRemElse.retainAll(annotationNode.getParent(AFTER).getAllChildren());

            if(commonAddElse.isEmpty() && commonRemElse.isEmpty()){
                return Optional.empty();
            }

            return Optional.of(new PatternMatch<>(this,
                    annotationNode.getLinesInDiff().fromInclusive(), removedElse.getLinesInDiff().toExclusive()
            ));
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
