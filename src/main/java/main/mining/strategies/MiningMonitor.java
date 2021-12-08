package main.mining.strategies;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import main.mining.DiffTreeMiningStrategy;
import org.pmw.tinylog.Logger;

import java.nio.file.Path;

public class MiningMonitor extends DiffTreeMiningStrategy {
    private final int msToWait;
    private long lastMeasurement;
    private long startTime;
    private int commitsProcessedTotal;

    public MiningMonitor(int seconds) {
        this.msToWait = 1000 * seconds;
    }

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);
        startTime = System.currentTimeMillis();
        lastMeasurement = startTime;
        commitsProcessedTotal = 0;
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        ++commitsProcessedTotal;
        long timeNow = System.currentTimeMillis();
        long msPassed = timeNow - lastMeasurement;

        if (msPassed >= msToWait) {
            reportProgress(timeNow - startTime);
            lastMeasurement = timeNow;
        }
    }

    @Override
    public void end() {
        reportProgress(System.currentTimeMillis() - startTime);
    }

    private void reportProgress(long msPassed) {
        float commitsPerSecond = commitsProcessedTotal / (msPassed / 1000F);
        // TODO: Format for float value
        Logger.info("Processed " + commitsProcessedTotal + " commits at about " + commitsPerSecond + " commits/s.");
    }
}
