package mining.strategies;

import mining.DiffTreeMiningResult;
import util.Assert;

public class CommitProcessTime {
    private static final String STR_DELIMITER = "___";
    private String hash;
    private String repoName;
    private long milliseconds;

    public CommitProcessTime(final String hash, final String reponame, long milliseconds) {
        set(hash, reponame, milliseconds);
    }

    public static CommitProcessTime Invalid(final String repoName) {
        return new CommitProcessTime("invalid", repoName, -1);
    }

    public void set(final String hash, long milliseconds) {
        set(hash, repoName, milliseconds);
    }

    public void set(final String hash, final String repoName, long milliseconds) {
        this.hash = hash;
        this.milliseconds = milliseconds;
        this.repoName = repoName;
    }

    public void set(final CommitProcessTime other) {
        set(other.hash, other.repoName, other.milliseconds);
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

    public static CommitProcessTime Unknown(final String repoName, long milliseconds) {
        return new CommitProcessTime(DiffTreeMiningResult.NO_REPO, repoName, milliseconds);
    }

    public static CommitProcessTime fromString(String text) {
        text = text.substring(0, text.length() - 2); // remove "ms" at end of string
        final String[] words = text.split(STR_DELIMITER);
        Assert.assertTrue(words.length == 3, "Ill-Formed input. Expected three words separated by \"" + STR_DELIMITER + "\" but got \"" + text + "\"!");
        final String hash = words[0];
        final String repoName = words[1];
        final long ms = Long.parseLong(words[2]);
        return new CommitProcessTime(hash, repoName, ms);
    }

    @Override
    public String toString() {
        return hash + STR_DELIMITER + repoName + STR_DELIMITER + milliseconds + "ms";
    }
}
