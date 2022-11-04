package org.variantsync.diffdetective.analysis.strategies;

import org.variantsync.diffdetective.analysis.monitoring.TaskCompletionMonitor;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;

import java.io.OutputStream;
import java.nio.file.Path;

/**
 * Analysis strategy that monitors the completion level of a task.
 * This is an adapter for {@link TaskCompletionMonitor}.
 * @author Paul Bittner
 */
public class AnalysisMonitor extends AnalysisStrategy {
    private final TaskCompletionMonitor monitor;

    /**
     * Creates a new AnalysisMonitor that prints the current status every <code>seconds</code> seconds.
     * @param seconds The amount of seconds to wait between reporting speed statistics.
     */
    public AnalysisMonitor(int seconds) {
        monitor = new TaskCompletionMonitor(seconds, TaskCompletionMonitor.LogProgress("commits"));
    }

    @Override
    public void start(Repository repo, Path outputPath) {
        super.start(repo, outputPath);
        monitor.start();
    }

    @Override
    public OutputStream onCommit(CommitDiff commit) {
        // FIXME This function is called before processing the commit.
        monitor.addFinishedTasks(1);

        return OutputStream.nullOutputStream();
    }

    @Override
    public void end() {
        monitor.reportProgress();
    }
}
