package org.variantsync.diffdetective.preliminary.pattern.elementary;

import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.variation.diff.DiffNode;

@Deprecated
public final class FeatureContextOfReconfiguration implements FeatureContextReverseEngineering<DiffNode<?>> {
    @Override
    public Pattern<DiffNode<?>> getPattern() {
        return ProposedEditClasses.Reconfiguration;
    }

    @Override
    public PatternMatch<DiffNode<?>> createMatch(DiffNode<?> codeNode) {
        final LineRange diffLines = codeNode.getLinesInDiff();
        return new PatternMatch<>(this,
                diffLines.fromInclusive(), diffLines.toExclusive()
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode<?>> patternMatch) {
        return new FeatureContext[0];
    }
}
