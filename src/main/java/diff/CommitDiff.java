package diff;

import org.eclipse.jgit.revwalk.RevCommit;

import java.util.ArrayList;
import java.util.List;

/**
 * Data class containing all PatchDiffs in a single commit.
 *
 * @author Sören Viegener
 */
public class CommitDiff {
    private final List<PatchDiff> patchDiffs;
    private final String parentCommitHash;
    private final String commitHash;
    final boolean merge;

    public CommitDiff(RevCommit commit, RevCommit parent) {
        this.commitHash = commit.getId().getName();
        this.parentCommitHash = parent.getId().getName();
        this.merge = commit.getParentCount() > 1;
        this.patchDiffs = new ArrayList<>();
    }

    public void addPatchDiff (PatchDiff patchDiff){
        patchDiffs.add(patchDiff);
    }

    public List<PatchDiff> getPatchDiffs() {
        return patchDiffs;
    }

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
        return "Diff from " + parentCommitHash + " (parent) to " + commitHash + " (child)";
    }
}
