package org.variantsync.diffdetective.diff;

import org.eclipse.jgit.diff.DiffEntry;
import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;

public interface GitPatch extends DiffTreeSource, Diff {
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

    DiffEntry.ChangeType getChangeType();
    String getFileName();
    String getCommitHash();
    String getParentCommitHash();
    GitPatch shallowClone();
}
