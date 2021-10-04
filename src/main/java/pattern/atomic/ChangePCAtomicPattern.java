package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.Optional;

public class ChangePCAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "ChangePC";

    public ChangePCAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public Optional<PatternMatch<DiffNode>> match(DiffNode codeNode) {
        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            final Lines diffLines = codeNode.getLinesInDiff();
            PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                    diffLines.getFromInclusive(), diffLines.getToExclusive()
            );
            if (addAmount > 0 && remAmount > 0){
                return Optional.of(patternMatch);
            } else if(addAmount == 0 && remAmount == 0 && codeNode.getAfterDepth() == codeNode.getBeforeDepth()){
                DiffNode currentBefore = codeNode.getBeforeParent();
                DiffNode currentAfter = codeNode.getAfterParent();
                boolean same = true;
                while(!currentBefore.isRoot() || !currentAfter.isRoot()){
                    if(currentBefore != currentAfter){
                        same = false;
                        break;
                    }
                    currentAfter = currentAfter.getAfterParent();
                    currentBefore = currentBefore.getBeforeParent();
                }
                if(!same) {
                    return Optional.of(patternMatch);
                }
            }
        }

        return Optional.empty();
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
