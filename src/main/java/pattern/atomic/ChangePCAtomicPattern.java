package pattern.atomic;

import analysis.data.PatternMatch;
import diff.Lines;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import evaluation.FeatureContext;
import pattern.AtomicPattern;

public class ChangePCAtomicPattern extends AtomicPattern {
    public static final String PATTERN_NAME = "ChangePC";

    public ChangePCAtomicPattern() {
        super(PATTERN_NAME, DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        int addAmount = codeNode.getAddAmount();
        int remAmount = codeNode.getRemAmount();
        if (addAmount > 0 && remAmount > 0){
            return true;
        } else if (addAmount == 0 && remAmount == 0 && codeNode.getAfterDepth() == codeNode.getBeforeDepth()) {
            DiffNode currentBefore = codeNode.getBeforeParent();
            DiffNode currentAfter = codeNode.getAfterParent();
            while(!currentBefore.isRoot() || !currentAfter.isRoot()) {
                if(currentBefore != currentAfter) {
                    // paths are not equal so pcs changed
                    // TODO: We should not check after and before paths but rather before and after pc shouldnt we?
                    return true;
                }
                currentAfter = currentAfter.getAfterParent();
                currentBefore = currentBefore.getBeforeParent();
            }
        }

        return false;
    }

    @Override
    public PatternMatch<DiffNode> createMatchOnCodeNode(DiffNode codeNode) {
        final Lines diffLines = codeNode.getLinesInDiff();
        return new PatternMatch<>(this,
                diffLines.getFromInclusive(), diffLines.getToExclusive()
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[0];
    }
}
