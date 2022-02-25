package mining.strategies;

import util.Assert;

public class CommitProcessTime {
    private static final String STR_DELIMITER = " with ";
    private String hash;
    private double seconds;

    public CommitProcessTime(final String hash, double seconds) {
        set(hash, seconds);
    }

    public void set(final String hash, double seconds) {
        this.hash = hash;
        this.seconds = seconds;
    }

    public void set(final CommitProcessTime other) {
        set(other.hash, other.seconds);
    }

    public String hash() {
        return hash;
    }

    public double seconds() {
        return seconds;
    }

    public static CommitProcessTime min(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.seconds < b.seconds) {
            return a;
        }
        return b;
    }

    public static CommitProcessTime max(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.seconds < b.seconds) {
            return b;
        }
        return a;
    }

    public static CommitProcessTime Unknown(double seconds) {
        return new CommitProcessTime("Unknown", seconds);
    }

    public static CommitProcessTime fromString(final String text) {
        final String[] words = text.split(STR_DELIMITER);
        Assert.assertTrue(words.length == 2, "Ill-Formed input. Expected two words separated by \"" + STR_DELIMITER + "\" but got \"" + text + "\"!");
        final String hash = words[0];
        final double seconds = Double.parseDouble(words[1]);
        return new CommitProcessTime(hash, seconds);
    }

    @Override
    public String toString() {
        return hash + STR_DELIMITER + seconds;
    }
}
