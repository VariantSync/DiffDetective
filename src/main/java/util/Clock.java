package util;

public class Clock {
    private long startTime;

    public void start() {
        startTime = System.currentTimeMillis();
    }

    public double getPassedSeconds() {
        final long msPassed = System.currentTimeMillis() - startTime;
        return (msPassed / 1000.0);
    }

    public String printPassedSeconds() {
        return getPassedSeconds() + "s";
    }
}
