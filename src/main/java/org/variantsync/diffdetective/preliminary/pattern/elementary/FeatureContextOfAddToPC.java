package org.variantsync.diffdetective.preliminary.pattern.elementary;

import org.prop4j.Node;
import org.variantsync.diffdetective.util.LineRange;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;
import org.variantsync.diffdetective.preliminary.pattern.Pattern;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;

@Deprecated
public final class FeatureContextOfAddToPC implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public Pattern<DiffNode> getPattern() {
        return ProposedEditClasses.AddToPC;
    }

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode codeNode) {
        final Node fm = codeNode.getParent(AFTER).getFeatureMapping(AFTER);
        final LineRange diffLines = codeNode.getLinesInDiff();

        return new PatternMatch<>(this,
                diffLines.fromInclusive(), diffLines.toExclusive(), fm
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(patternMatch.getFeatureMappings()[0])
        };
    }
}
