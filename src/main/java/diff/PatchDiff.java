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
public class PatchDiff {
    private final String fullDiff;
    private final DiffTree diffTree;
    private final CommitDiff commitDiff;
    private final DiffEntry.ChangeType changeType;
    private final String path;

    public PatchDiff(CommitDiff commitDiff, DiffEntry diffEntry, String fullDiff,
                     DiffTree diffTree) {
        this.commitDiff = commitDiff;
        this.changeType = diffEntry.getChangeType();
        this.path = diffEntry.getNewPath();
        this.fullDiff = fullDiff;
        this.diffTree = diffTree;
    }

    public CommitDiff getCommitDiff() {
        return this.commitDiff;
    }

    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    public String getFileName() {
        return path;
    }

    public String getFullDiff() {
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
        return commitDiff.getAbbreviatedCommitHash() + ", " + path;
    }
}
