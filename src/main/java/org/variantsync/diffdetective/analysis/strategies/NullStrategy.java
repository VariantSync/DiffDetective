package org.variantsync.diffdetective.analysis.strategies;

import java.io.OutputStream;

import org.variantsync.diffdetective.diff.CommitDiff;

/**
 * Empty strategy that does nothing.
 * @author Paul Bittner
 */
public class NullStrategy extends AnalysisStrategy {
    @Override
    public OutputStream onCommit(CommitDiff commit) {
        return OutputStream.nullOutputStream();
    }

    @Override
    public void end() {

    }
}
