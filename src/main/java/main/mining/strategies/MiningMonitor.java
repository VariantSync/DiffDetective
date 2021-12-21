package main.mining.strategies;

import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import main.mining.monitoring.TaskCompletionMonitor;

import java.nio.file.Path;

public class MiningMonitor extends DiffTreeMiningStrategy {
    private final TaskCompletionMonitor monitor;

    public MiningMonitor(int seconds) {
        monitor = new TaskCompletionMonitor(seconds, TaskCompletionMonitor.LogProgress("commits"));
    }

    @Override
    public void start(Repository repo, Path outputPath, DiffTreeLineGraphExportOptions options) {
        super.start(repo, outputPath, options);
        monitor.start();
    }

    @Override
    public void onCommit(CommitDiff commit, String lineGraph) {
        monitor.addFinishedTasks(1);
    }

    @Override
    public void end() {
        monitor.reportProgress();
    }
}
