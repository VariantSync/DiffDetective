package mining.strategies;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import util.IO;

import java.nio.file.Path;

public class MineAllThenExport extends DiffTreeMiningStrategy {
    private StringBuilder waitForAll;

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);
        waitForAll = new StringBuilder();
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        waitForAll.append(lineGraph);
    }

    @Override
    public void end() {
        final String lineGraph = waitForAll.toString();
//        Logger.info("Writing file " + outputPath);
        IO.tryWrite(outputPath, lineGraph);
    }
}
