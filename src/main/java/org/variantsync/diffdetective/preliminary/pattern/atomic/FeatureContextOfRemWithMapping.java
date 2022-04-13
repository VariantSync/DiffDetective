package org.variantsync.diffdetective.preliminary.pattern.atomic;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.Lines;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.pattern.EditPattern;
import org.variantsync.diffdetective.pattern.atomic.proposed.ProposedAtomicPatterns;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

@Deprecated
public final class FeatureContextOfRemWithMapping implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public EditPattern<DiffNode> getPattern() {
        return ProposedAtomicPatterns.RemWithMapping;
    }

    @Override
    public PatternMatch<DiffNode> createMatch(DiffNode codeNode) {
        final Node fm = codeNode.getBeforeParent().getBeforeFeatureMapping();
        final Lines diffLines = codeNode.getLinesInDiff();

        return new PatternMatch<>(this,
                diffLines.getFromInclusive(), diffLines.getToExclusive(), fm
        );
    }

    @Override
    public FeatureContext[] getFeatureContexts(PatternMatch<DiffNode> patternMatch) {
        return new FeatureContext[]{
                new FeatureContext(null),
                new FeatureContext(patternMatch.getFeatureMappings()[0], true)
        };
    }
}
