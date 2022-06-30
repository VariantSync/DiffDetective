package org.variantsync.diffdetective.preliminary;

import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.PatchDiff;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing information about the differences in the commit history of a git repository.
 *
 * @author Sören Viegener
 */
@Deprecated
public class GitDiff {

	/**
	 * A list of all {@link CommitDiff CommitDiffs}.
	 */
    private final List<CommitDiff> commitDiffs;
    
    /**
     * The amount of all commits.
     */
    private int commitAmount;

    public GitDiff() {
        this.commitDiffs = new ArrayList<>();
    }

    /**
     * Add a {@link CommitDiff} to the list of {@link CommitDiff CommitDiffs}.
     * 
     * @param commitDiff The {@link CommitDiff} to e added
     */
    public void addCommitDiff(CommitDiff commitDiff) {
        this.commitDiffs.add(commitDiff);
    }

    public int getPatchAmount() {
        return this.commitDiffs.stream().mapToInt(CommitDiff::getPatchAmount).sum();
    }

    /**
     * @return The amount of all patches of all commits.
     */
    public int getCommitDiffAmount() {
        return this.commitDiffs.size();
    }

    public void setCommitAmount(int commitAmount) {
        this.commitAmount = commitAmount;
    }

    /**
     * @return The amount of {@link CommitDiff CommitDiffs}.
     */
    public int getCommitAmount() {
        return this.commitAmount;
    }

    /**
     * @return All {@link PatchDiff PatchDiffs} of all {@link CommitDiff CommitDiffs}.
     */
    public List<PatchDiff> getPatchDiffs() {
        List<PatchDiff> patchDiffs = new ArrayList<>(getPatchAmount());
        for (CommitDiff commitDiff : commitDiffs) {
            patchDiffs.addAll(commitDiff.getPatchDiffs());
        }
        return patchDiffs;
    }

    /**
     * @return The list of all {@link CommitDiff CommitDiffs}.
     */
    public List<CommitDiff> getCommitDiffs() {
        return commitDiffs;
    }
}
