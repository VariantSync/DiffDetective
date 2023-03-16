package org.variantsync.diffdetective.diff.git;

import org.apache.commons.io.FilenameUtils;
import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;

/**
 * Data class containing information about a single patch (i.e., the differences in a single file).
 *
 * Contains a DiffTree of the patch.
 *
 * @author Sören Viegener, Paul Bittner
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
     * Path of the file before modification.
     */
    private final String oldPath;
    /**
     * Path of the file after modification.
     */
    private final String newPath;

    /**
     * Creates a new PatchDiff.
     * @param commitDiff The changes of a commit this patch belongs to.
     * @param diffEntry The diff entry from jgit from which this PatchDiff was produced.
     * @param fullDiff The diff of this patch as text. Might be empty.
     * @param diffTree The {@link DiffTree} that describes this patch.
     */
    public PatchDiff(CommitDiff commitDiff, DiffEntry diffEntry, String fullDiff,
                     DiffTree diffTree) {
        this.commitDiff = commitDiff;
        this.changeType = diffEntry.getChangeType();
        this.oldPath = diffEntry.getOldPath();
        this.newPath = diffEntry.getNewPath();
        this.fullDiff = fullDiff;
        this.diffTree = diffTree;
        if (this.diffTree != null) {
            this.diffTree.setSource(this);
        }
    }

    /**
     * Returns the corresponding CommitDiff, which this patch is part of.
     */
    public CommitDiff getCommitDiff() {
        return this.commitDiff;
    }

    /**
     * Returns the extension of the file this patch is modifying.
     */
    public String getFileExtension(Time time) {
        return FilenameUtils.getExtension(getFileName(time)).toLowerCase();
    }

    @Override
    public DiffEntry.ChangeType getChangeType() {
        return changeType;
    }

    @Override
    public String getFileName(Time time) {
        if (time == Time.BEFORE) {
            return oldPath;
        }else {
            return newPath;
        }
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

    /**
     * Returns the DiffTree for this patch.
     */
    public DiffTree getDiffTree() {
        return diffTree;
    }

    /**
     * Returns whether this PatchDiff is a valid patch.
     * A patch is valid if it has a DiffTree.
     */
    public boolean isValid() {
        return diffTree != null;
    }

    @Override
    public String toString() {
        return newPath + "@ " + commitDiff;
    }

    @Override
    public GitPatch shallowClone() {
        return new GitPatch.SimpleGitPatch(getDiff(), getChangeType(), getFileName(Time.BEFORE), getFileName(Time.AFTER), getCommitHash(), getParentCommitHash());
    }
}
