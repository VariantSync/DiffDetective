package diff;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing information about the differences in the commit history of a git repository.
 *
 * @author Sören Viegener
 */
public class GitDiff {
    private final List<CommitDiff> commitDiffs;
    private int commitAmount;

    public GitDiff() {
        this.commitDiffs = new ArrayList<>();
    }

    public void addCommitDiff(CommitDiff commitDiff) {
        this.commitDiffs.add(commitDiff);
    }

    public int getPatchAmount() {
        return this.commitDiffs.stream().mapToInt(CommitDiff::getPatchAmount).sum();
    }

    public int getCommitDiffAmount() {
        return this.commitDiffs.size();
    }

    public void setCommitAmount(int commitAmount) {
        this.commitAmount = commitAmount;
    }

    public int getCommitAmount() {
        return this.commitAmount;
    }

    public List<PatchDiff> getPatchDiffs() {
        List<PatchDiff> patchDiffs = new ArrayList<>(getPatchAmount());
        for (CommitDiff commitDiff : commitDiffs) {
            patchDiffs.addAll(commitDiff.getPatchDiffs());
        }
        return patchDiffs;
    }

    public List<CommitDiff> getCommitDiffs() {
        return commitDiffs;
    }
}
