package diff.data;

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
    private final String commitHash;
    final boolean merge;

    public CommitDiff(RevCommit commit) {
        this.commitHash = commit.getId().getName();
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
}
