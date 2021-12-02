package main.mining;

import datasets.Repository;
import diff.CommitDiff;
import diff.serialize.LineGraphExport;
import org.pmw.tinylog.Logger;
import util.IO;

import java.nio.file.Path;

public class MineAllThenExport extends DiffTreeMiningStrategy {
    private StringBuilder waitForAll;

    @Override
    public void start(Repository repo, Path outputPath, LineGraphExport.Options options) {
        super.start(repo, outputPath, options);
        waitForAll = new StringBuilder();
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        waitForAll.append(lineGraph);
    }

    @Override
    public String end() {
        final String lineGraph = waitForAll.toString();
        Logger.info("Writing file " + outputPath);
        IO.tryWrite(outputPath, lineGraph);
        return lineGraph;
    }
}