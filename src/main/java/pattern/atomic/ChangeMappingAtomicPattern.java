package pattern.atomic;

import analysis.data.PatternMatch;
import diff.data.DiffNode;
import evaluation.FeatureContext;

import java.util.ArrayList;
import java.util.List;

public class ChangeMappingAtomicPattern extends AtomicPattern{
    public static final String PATTERN_NAME = "ChangeMapping";

    public ChangeMappingAtomicPattern() {
        this.name = PATTERN_NAME;
    }

    @Override
    public List<PatternMatch> getMatches(DiffNode codeNode) {
        List<PatternMatch> patternMatches = new ArrayList<>();

        if (codeNode.isNon()){
            int addAmount = codeNode.getAddAmount();
            int remAmount = codeNode.getRemAmount();
            PatternMatch patternMatch = new PatternMatch(this,
                    codeNode.getFromLine(), codeNode.getToLine()
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
    public FeatureContext[] getFeatureContexts(PatternMatch patternMatch) {
        return new FeatureContext[0];
    }
}
