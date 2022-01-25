package diff;

import diff.difftree.DiffTree;
import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;

/**
 * Data class containing information about a single patch (i.e. the differences in a single file).
 *
 * Contains a DiffTree of the patch
 *
 * @author Sören Viegener
 */
public class PatchDiff implements GitPatch {
    private final String fullDiff;
    private final DiffTree diffTree;
    
    /**
     * The commit the patch belongs to.
     */
    private final CommitDiff commitDiff;
    
    /**
     * The general change type of a single file. See {@link DiffEntry.ChangeType Type}.
     */
    private final DiffEntry.ChangeType changeType;
    
    /**
     * Path of the file (patch) that has been modified.
     */
    private final String path;

    public PatchDiff(CommitDiff commitDiff, DiffEntry diffEntry, String fullDiff,
                     DiffTree diffTree) {
        this.commitDiff = commitDiff;
        this.changeType = diffEntry.getChangeType();
        this.path = diffEntry.getNewPath();
        this.fullDiff = fullDiff;
        this.diffTree = diffTree;
        if (this.diffTree != null) {
            this.diffTree.setSource(this);
        }
    }

    public CommitDiff getCommitDiff() {
        return this.commitDiff;
    }

    @Override
    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String getFileName() {
        return path;
    }

    @Override
    public String getCommitHash() {
        return commitDiff.getCommitHash();
    }

    @Override
    public String getParentCommitHash() {
        return commitDiff.getParentCommitHash();
    }

    @Override
    public String getDiff() {
        return fullDiff;
    }

    public String getFileExtension() {
        return FilenameUtils.getExtension(getFileName()).toLowerCase();
    }

    public DiffTree getDiffTree() {
        return diffTree;
    }

    public boolean isValid() {
        return diffTree != null;
    }

    @Override
    public String toString() {
        return path + "@ " + commitDiff;
    }

    public GitPatch shallowClone() {
        return new GitPatch.SimpleGitPatch(getDiff(), getChangeType(), getFileName(), getCommitHash(), getParentCommitHash());
    }
}
