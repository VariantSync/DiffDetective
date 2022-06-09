package org.variantsync.diffdetective.analysis.monitoring;

import org.tinylog.Logger;

import java.util.function.Consumer;

public class TaskCompletionMonitor {
    public record TimeInfo(int completedTasks, float passedSeconds, float tasksPerSecond) {}
    private final Consumer<TimeInfo> progressReporter;
    private final int msToWait;
    private long lastMeasurement;
    private long startTime;
    private int tasksDone;

    public TaskCompletionMonitor(int seconds, final Consumer<TimeInfo> progressReporter) {
        this.msToWait = 1000 * seconds;
        this.progressReporter = progressReporter;
    }

    public void start() {
        startTime = System.currentTimeMillis();
        lastMeasurement = startTime;
        tasksDone = 0;
    }

    public void addFinishedTasks(int numberOfFinishedTasks) {
        tasksDone += numberOfFinishedTasks;

        final long timeNow = System.currentTimeMillis();
        final long msPassed = timeNow - lastMeasurement;

        if (msPassed >= msToWait) {
            reportProgress();
        }
    }

    public void reportProgress() {
        final long timeNow = System.currentTimeMillis();
        final long msPassedTotal = timeNow - startTime;
        final float sPassedTotal = msPassedTotal / 1000.0f;
        final float tasksPerSecond = tasksDone / sPassedTotal;
        lastMeasurement = timeNow;

        progressReporter.accept(new TimeInfo(tasksDone, sPassedTotal, tasksPerSecond));
    }

    public static Consumer<TimeInfo> LogProgress(final String tasksName) {
        return time -> Logger.info("Processed {} {} after {}s at about {}{}/s.",
                        time.completedTasks,
                        tasksName,
                        String.format("%.2f", time.passedSeconds),
                        String.format("%.2f", time.tasksPerSecond),
                        tasksName);
    }
}
