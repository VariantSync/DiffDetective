package mining.strategies;

import diff.CommitDiff;

public class NullStrategy extends DiffTreeMiningStrategy {
    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {

    }

    @Override
    public void end() {

    }
}
