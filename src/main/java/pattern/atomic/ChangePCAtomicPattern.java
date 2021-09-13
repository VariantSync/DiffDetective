package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class ChangePCAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "ChangePC";

    public ChangePCAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch<DiffNode>> getMatches(DiffNode codeNode) {
        List<PatternMatch<DiffNode>> patternMatches = new ArrayList<>();

        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            final Lines diffLines = codeNode.getLinesInDiff();
            PatternMatch<DiffNode> patternMatch = new PatternMatch<>(this,
                    diffLines.getFromInclusive(), diffLines.getToExclusive()
            );
            if (addAmount > 0 && remAmount > 0){
                patternMatches.add(patternMatch);
            }else if(addAmount == 0 && remAmount == 0 && codeNode.getAfterDepth() == codeNode.getBeforeDepth()){
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
                if(!same){
                    patternMatches.add(patternMatch);
                }
            }
        }
        return patternMatches;
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
