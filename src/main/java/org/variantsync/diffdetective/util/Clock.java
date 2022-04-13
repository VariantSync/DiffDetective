package org.variantsync.diffdetective.util;

public class Clock {
    private long startTime;

    public Clock() {
        start();
    }

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public long getPassedMilliseconds() {
        return System.currentTimeMillis() - startTime;
    }

    public double getPassedSeconds() {
        return toSeconds(getPassedMilliseconds());
    }

    public String printPassedSeconds() {
        return printPassedSeconds(getPassedSeconds());
    }

    public static double toSeconds(long milliseconds) {
        return milliseconds / 1000.0;
    }

    public static String printPassedSeconds(double seconds) {
        return seconds + "s";
    }
}
