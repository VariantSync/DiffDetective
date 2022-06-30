package org.variantsync.diffdetective.diff;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the changes made by a commit relative to another commit.
 * A CommitDiff stores the current commit's id as well as we parent commit id.
 * The diff represents the changes made by the current comment to the parent commit.
 * The changes are represented by a list of {@link PatchDiff}s as a commit modifies a set
 * of files.
 * This is a data class that does not provide functionality.
 *
 * @author Sören Viegener, Paul Bittner
 */
public class CommitDiff {
	/**
	 * A list of all {@link PatchDiff PatchDiffs} of a {@link CommitDiff}.
	 */
    private final List<PatchDiff> patchDiffs;

    /**
     * The hash of the parent commit.
     */
    private final String parentCommitHash;

    /**
     * The hash of the current commit.
     */
    private final String commitHash;
    
    final boolean merge;

    /**
     * Creates a new diff for the following two commits.
     * The resulting CommitDiff is empty meaning that it does not contain any PatchDiffs.
     * @param commit The commit whose changes are stored relative to parent.
     * @param parent The commit to which changes are compared.
     * @see PatchDiff
     */
    public CommitDiff(RevCommit commit, RevCommit parent) {
        this.commitHash = commit.getId().getName();
        this.parentCommitHash = parent.getId().getName();
        this.merge = commit.getParentCount() > 1;
        this.patchDiffs = new ArrayList<>();
    }

    /**
     * Add a {@link PatchDiff}.
     * The given diff should belong to the changes between the commits of this CommitDiff.
     * 
     * @param patchDiff The {@link PatchDiff PatchDiff} to be added
     */
    public void addPatchDiff(PatchDiff patchDiff){
        patchDiffs.add(patchDiff);
    }

    /**
     * Returns the list of all {@link PatchDiff PatchDiffs} that belong to this {@link CommitDiff}.
     */
    public List<PatchDiff> getPatchDiffs() {
        return patchDiffs;
    }

    /**
     * Returns the amount of {@link PatchDiff PatchDiffs} that belong to this {@link CommitDiff}.
     */
    public int getPatchAmount() {
        return patchDiffs.size();
    }

    /**
     * Returns true iff this commit is a merge commit.
     */
    public boolean isMergeCommit() {
        return merge;
    }

    /**
     * Returns the first n characters of this commit's hash.
     * @param length The number of characters to include in the commit hash.
     */
    public String getAbbreviatedCommitHash(int length) {
        return commitHash.substring(0, length);
    }

    /**
     * Returns this commit's hash abbreviated to the first eight characters.
     * The first eight characters are usually unique if a commit history does not become too large.
     */
    public String getAbbreviatedCommitHash() {
        return getAbbreviatedCommitHash(8);
    }

    /**
     * Returns the full commit hash of this commit.
     */
    public String getCommitHash() {
        return commitHash;
    }

    /**
     * Returns the fill commit hash of the parent commit.
     * @see CommitDiff#CommitDiff
     */
    public String getParentCommitHash() {
        return parentCommitHash;
    }

    @Override
    public String toString() {
        return "commit from " + parentCommitHash + " (parent) to " + commitHash + " (child)";
    }
}
