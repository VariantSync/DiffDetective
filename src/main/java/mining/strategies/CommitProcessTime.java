package mining.strategies;

import util.Assert;

public class CommitProcessTime {
    private static final String STR_DELIMITER = " with ";
    private String hash;
    private long milliseconds;

    public CommitProcessTime(final String hash, long milliseconds) {
        set(hash, milliseconds);
    }

    public void set(final String hash, long milliseconds) {
        this.hash = hash;
        this.milliseconds = milliseconds;
    }

    public void set(final CommitProcessTime other) {
        set(other.hash, other.milliseconds);
    }

    public String hash() {
        return hash;
    }

    public long milliseconds() {
        return milliseconds;
    }

    public static CommitProcessTime min(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.milliseconds < b.milliseconds) {
            return a;
        }
        return b;
    }

    public static CommitProcessTime max(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.milliseconds < b.milliseconds) {
            return b;
        }
        return a;
    }

    public static CommitProcessTime Unknown(long milliseconds) {
        return new CommitProcessTime("Unknown", milliseconds);
    }

    public static CommitProcessTime fromString(final String text) {
        final String[] words = text.split(STR_DELIMITER);
        Assert.assertTrue(words.length == 2, "Ill-Formed input. Expected two words separated by \"" + STR_DELIMITER + "\" but got \"" + text + "\"!");
        final String hash = words[0];
        final long ms = Long.parseLong(words[1]);
        return new CommitProcessTime(hash, ms);
    }

    @Override
    public String toString() {
        return hash + STR_DELIMITER + milliseconds;
    }
}
