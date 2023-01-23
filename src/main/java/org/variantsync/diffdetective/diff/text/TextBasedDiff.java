package org.variantsync.diffdetective.diff.text;

/**
 * Interface for diffs to text that allows accessing the diff as text.
 * @author Paul Bittner
 */
public interface TextBasedDiff {
    /**
     * Returns this diff as plain text.
     */
    String getDiff();
}
