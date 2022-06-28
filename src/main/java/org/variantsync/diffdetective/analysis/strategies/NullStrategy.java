package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.diff.CommitDiff;

/**
 * Empty strategy that does nothing.
 * @author Paul Bittner
 */
public class NullStrategy extends AnalysisStrategy {
    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {

    }

    @Override
    public void end() {

    }
}
