package main.mining.monitoring;

import org.pmw.tinylog.Logger;

import java.util.function.BiConsumer;

public class TaskCompletionMonitor {
    private final BiConsumer<Integer, Float> progressReporter;
    private final int msToWait;
    private long lastMeasurement;
    private long startTime;
    private int tasksDone;

    public TaskCompletionMonitor(int seconds, final BiConsumer<Integer, Float> progressReporter) {
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
        long timeNow = System.currentTimeMillis();
        long msPassed = timeNow - lastMeasurement;

        if (msPassed >= msToWait) {
            reportProgress();
        }
    }

    public void reportProgress() {
        final long timeNow = System.currentTimeMillis();
        final long msPassed = timeNow - startTime;
        final float tasksPerSecond = tasksDone / (msPassed / 1000F);
        lastMeasurement = timeNow;

        progressReporter.accept(tasksDone, tasksPerSecond);
    }

    public static BiConsumer<Integer, Float> LogProgress(final String tasksName) {
        return (finishedTasks, tasksPerSecond) -> Logger.info(
                "Processed " + finishedTasks + " " + tasksName
                + " at about " + tasksPerSecond + tasksName + "/s.");
    }
}
