package org.variantsync.diffdetective.preliminary.pattern.semantic;

import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Stream;

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

            Collection<DiffNode> annotationChildren = annotationNode.getParent(AFTER).getAllChildrenSet();
            Stream<DiffNode> commonAddElse = annotationNode.getAllChildrenStream().filter(annotationChildren::contains);
            Stream<DiffNode> commonRemElse = removedElse.getAllChildrenStream().filter(annotationChildren::contains);

            if(commonAddElse.limit(1).count() == 0 && commonRemElse.limit(1).count() == 0){
                return Optional.empty();
            }

            return Optional.of(new PatternMatch<>(this,
                    annotationNode.getLinesInDiff().getFromInclusive(), removedElse.getLinesInDiff().getToExclusive()
            ));
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
