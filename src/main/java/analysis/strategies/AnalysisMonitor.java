package analysis.strategies;

import analysis.monitoring.TaskCompletionMonitor;
import datasets.Repository;
import diff.CommitDiff;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;

import java.nio.file.Path;

public class AnalysisMonitor extends AnalysisStrategy {
    private final TaskCompletionMonitor monitor;

    public AnalysisMonitor(int seconds) {
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
