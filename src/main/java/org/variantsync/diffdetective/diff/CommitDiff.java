package org.variantsync.diffdetective.diff;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing all PatchDiffs in a single commit.
 *
 * @author Sören Viegener
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

    public CommitDiff(RevCommit commit, RevCommit parent) {
        this.commitHash = commit.getId().getName();
        this.parentCommitHash = parent.getId().getName();
        this.merge = commit.getParentCount() > 1;
        this.patchDiffs = new ArrayList<>();
    }

    /**
     * Add a {@link PatchDiff}.
     * 
     * @param patchDiff The {@link PatchDiff PatchDiff} to be added
     */
    public void addPatchDiff(PatchDiff patchDiff){
        patchDiffs.add(patchDiff);
    }

    /**
     * @return The list of {@link PatchDiff PatchDiffs} that belong to this {@link CommitDiff}
     */
    public List<PatchDiff> getPatchDiffs() {
        return patchDiffs;
    }

    /**
     * @return The amount of {@link PatchDiff PatchDiffs} that belong to this {@link CommitDiff}
     */
    public int getPatchAmount() {
        return patchDiffs.size();
    }

    public boolean isMergeCommit() {
        return merge;
    }

    public String getAbbreviatedCommitHash(int length) {
        return commitHash.substring(0, length);
    }

    public String getAbbreviatedCommitHash() {
        return getAbbreviatedCommitHash(8);
    }

    public String getCommitHash() {
        return commitHash;
    }

    public String getParentCommitHash() {
        return parentCommitHash;
    }

    @Override
    public String toString() {
        return "commit from " + parentCommitHash + " (parent) to " + commitHash + " (child)";
    }
}
