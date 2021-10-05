package pattern.semantic;

import analysis.data.PatternMatch;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;
import pattern.SemanticPattern;

import java.util.Collection;
import java.util.Optional;

public class MoveElseSemanticPattern extends SemanticPattern {
    public static final String PATTERN_NAME = "MoveElseSEM";

    public MoveElseSemanticPattern() {
        super(PATTERN_NAME);
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
            for(DiffNode parentsChild : annotationNode.getAfterParent().getAllChildren()){
                if(parentsChild.isElse() && parentsChild.isRem()){
                    removedElse = parentsChild;
                    break;
                }
            }

            if(removedElse == null){
                return Optional.empty();
            }

            Collection<DiffNode> commonAddElse = annotationNode.getAllChildren();
            commonAddElse.retainAll(annotationNode.getAfterParent().getAllChildren());

            Collection<DiffNode> commonRemElse = removedElse.getAllChildren();
            commonRemElse.retainAll(annotationNode.getAfterParent().getAllChildren());

            if(commonAddElse.isEmpty() && commonRemElse.isEmpty()){
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
