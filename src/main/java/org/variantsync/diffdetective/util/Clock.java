package org.variantsync.diffdetective.util;

/** A clock counting the number of milliseconds since it was {@code start}ed. */
public class Clock {
    private long startTime;

    /** Constructs a {@code Clock} and {@link start}s it for convenience. */
    public Clock() {
        start();
    }

    /** The clock now. Previous start times are not remembered. */
    public void start() {
        startTime = System.currentTimeMillis();
    }

    /** Returns the number of milliseconds since {@link start}. */
    public long getPassedMilliseconds() {
        return System.currentTimeMillis() - startTime;
    }

    /** Returns the number of seconds since {@link start}. */
    public double getPassedSeconds() {
        return toSeconds(getPassedMilliseconds());
    }

    /**
     * Returns a human readable string containing the number seconds since {@link start} with a
     * unit.
     */
    public String printPassedSeconds() {
        return printPassedSeconds(getPassedSeconds());
    }

    /** Convert the number of milliseconds to a number of seconds. */
    public static double toSeconds(long milliseconds) {
        return milliseconds / 1000.0;
    }

    /** Returns a human readable string containing {@code seconds} with a unit. */
    public static String printPassedSeconds(double seconds) {
        return seconds + "s";
    }
}
