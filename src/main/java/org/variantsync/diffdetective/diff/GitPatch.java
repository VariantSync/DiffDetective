package org.variantsync.diffdetective.diff;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

/**
 * Interface for patches from a git repository.
 * A git patch is a {@link TextBasedDiff} from which {@link org.variantsync.diffdetective.diff.difftree.DiffTree DiffTrees} can be created.
 *
 */
public interface GitPatch extends DiffTreeSource, TextBasedDiff {
    /**
     * Minimal default implementation of {@link GitPatch}
     * @param getDiff The diff in text form.
     * @param getChangeType The change type of this patch (e.g., file insertion or modification).
     * @param getFileName The name of the patched file.
     * @param getCommitHash The hash of the commit introducing the change.
     * @param getParentCommitHash The hash of the parent commit regarding which the diff was created.
     */
    record SimpleGitPatch(String getDiff, DiffEntry.ChangeType getChangeType, String getFileName, String getCommitHash, String getParentCommitHash)
        implements GitPatch {
        @Override
        public GitPatch shallowClone() {
            return new SimpleGitPatch(getDiff, getChangeType, getFileName, getCommitHash, getParentCommitHash);
        }

        @Override
        public String toString() {
            return getFileName + "@ commit from " + getParentCommitHash + " (parent) to " + getCommitHash + " (child)";
        }
    }

    /**
     * Returns the change type of this patch (e.g., file insertion or modification).
     */
    DiffEntry.ChangeType getChangeType();

    /**
     * Returns the name of the patched file.
     */
    String getFileName();

    /**
     * Returns the hash of the commit introducing the change.
     */
    String getCommitHash();

    /**
     * Returns the hash of the parent commit regarding which the diff was created.
     */
    String getParentCommitHash();

    /**
     * Creates a shallow clone.
     * A shallow clone should return the very same values for the methods of this interface.
     * Other behaviour is unspecified.
     * @return A clone that acts equal with respect to this interface.
     */
    GitPatch shallowClone();
}
