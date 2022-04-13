package org.variantsync.diffdetective.preliminary.pattern.atomic;

import org.variantsync.diffdetective.diff.Lines;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.pattern.EditPattern;
import org.variantsync.diffdetective.pattern.atomic.proposed.ProposedAtomicPatterns;
import org.variantsync.diffdetective.preliminary.analysis.data.PatternMatch;
import org.variantsync.diffdetective.preliminary.evaluation.FeatureContext;
import org.variantsync.diffdetective.preliminary.pattern.FeatureContextReverseEngineering;

@Deprecated
public class FeatureContextOfUntouched implements FeatureContextReverseEngineering<DiffNode> {
    @Override
    public EditPattern<DiffNode> getPattern() {
        return ProposedAtomicPatterns.Untouched;
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