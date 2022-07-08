package org.variantsync.diffdetective.analysis;

import org.variantsync.diffdetective.util.Assert;

/**
 * Stores the time it took to process a single commit in a given repository in milliseconds.
 * @author Paul Bittner
 */
public class CommitProcessTime {
    private static final String STR_DELIMITER = "___";
    private String hash;
    private String repoName;
    private long milliseconds;

    /**
     * Creates a new CommitProcessTime that stores that the commit the given has in the given repository took
     * the given amount of milliseconds to process.
     * @param hash The hash of the commit that was processed.
     * @param reponame The name of the repository from which the commit was taken.
     * @param milliseconds The time in milliseconds that were required to process the given commit.
     */
    public CommitProcessTime(final String hash, final String reponame, long milliseconds) {
        set(hash, reponame, milliseconds);
    }

    /**
     * Creates an invalid count for the given repo.
     * You may want to use this when no commits where analyzed at all.
     * @param repoName Name of the repository for which to create an invalid commit time.
     * @return A commit process time that does not refer to any commit and reports an illegal amount of milliseconds.
     */
    public static CommitProcessTime Invalid(final String repoName) {
        return new CommitProcessTime("invalid", repoName, -1);
    }

    /**
     * Creates an count for an unkown commit in the given repository.
     * You may want to use this when you need an initial value for a variable.
     * @param repoName Name of the repository for which to create an unknown commit time.
     * @return milliseconds The time in milliseconds that were required to process the unknown commit.
     */
    public static CommitProcessTime Unknown(final String repoName, long milliseconds) {
        return new CommitProcessTime(AnalysisResult.NO_REPO, repoName, milliseconds);
    }

    /**
     * Resets this commit time to the given commit hash and milliseconds.
     * @param hash The hash of the commit that was processed.
     * @param milliseconds The time in milliseconds that were required to process the given commit.
     */
    public void set(final String hash, long milliseconds) {
        set(hash, repoName, milliseconds);
    }

    /**
     * Completely resets this commit time.
     * @param hash The hash of the commit that was processed.
     * @param reponame The name of the repository from which the commit was taken.
     * @param milliseconds The time in milliseconds that were required to process the given commit.
     */
    public void set(final String hash, final String reponame, long milliseconds) {
        this.hash = hash;
        this.milliseconds = milliseconds;
        this.repoName = reponame;
    }

    /**
     * Sets all values of this object to the values of the given time.
     * Both objects can be seen as semantically equivalent afterwards.
     * @param other Other time whose value to copy into this one.
     */
    public void set(final CommitProcessTime other) {
        set(other.hash, other.repoName, other.milliseconds);
    }

    /**
     * Returns the commit hash.
     * @return the commit hash.
     */
    public String hash() {
        return hash;
    }

    /**
     * The amount of milliseconds that were required to process this commit.
     * @return the amount of milliseconds that were required to process this commit.
     */
    public long milliseconds() {
        return milliseconds;
    }

    /**
     * Computes the minimum of two {@link CommitProcessTime}s.
     * @param a First argument.
     * @param b Second argument.
     * @return Minimum of the given two times regarding the {@link CommitProcessTime#milliseconds()}.
     */
    public static CommitProcessTime min(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.milliseconds < b.milliseconds) {
            return a;
        }
        return b;
    }

    /**
     * Computes the maximum of two {@link CommitProcessTime}s.
     * @param a First argument.
     * @param b Second argument.
     * @return Maximum of the given two times regarding the {@link CommitProcessTime#milliseconds()}.
     */
    public static CommitProcessTime max(final CommitProcessTime a, final CommitProcessTime b) {
        if (a.milliseconds < b.milliseconds) {
            return b;
        }
        return a;
    }

    /**
     * Parses a {@link CommitProcessTime} from a string that was produced by {@link CommitProcessTime#toString()}.
     * @param text The text to parse to a commit process time´.
     * @return The parsed CommitProcessTime.
     * @throws AssertionError When the input is ill-formed.
     */
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
