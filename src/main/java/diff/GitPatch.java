package diff;

import diff.difftree.DiffTreeSource;
import org.eclipse.jgit.diff.DiffEntry;

public interface GitPatch extends DiffTreeSource, Diff {
    record SimpleGitPatch(String getDiff, DiffEntry.ChangeType getChangeType, String getFileName, String getCommitHash, String getParentCommitHash)
        implements GitPatch {
        @Override
        public GitPatch shallowClone() {
            return new SimpleGitPatch(getDiff, getChangeType, getFileName, getCommitHash, getParentCommitHash);
        }
    }

    DiffEntry.ChangeType getChangeType();
    String getFileName();
    String getCommitHash();
    String getParentCommitHash();
    GitPatch shallowClone();
}
