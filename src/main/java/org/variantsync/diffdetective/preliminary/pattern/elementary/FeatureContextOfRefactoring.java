package org.variantsync.diffdetective.preliminary.pattern.elementary;

import org.variantsync.diffdetective.diff.Lines;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

@Deprecated
public final class FeatureContextOfRefactoring implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public Pattern<DiffNode> getPattern() {
        return ProposedEditClasses.Refactoring;
    }

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode codeNode) {
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
