package org.variantsync.diffdetective.diff.result;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.util.Assert;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * The result of processing all diffs of a commit.
 * @param diff A CommitDiff upon a (partially) successful computation or nothing otherwise.
 * @param errors A list of errors that occured while processing the commit's diffs.
 */
public record CommitDiffResult(Optional<CommitDiff> diff, List<DiffError> errors) {
    /**
     * Creates a result that indicates failure from a single error.
     * The result will hold no diff and a list containing the single error.
     * @param error The error that occurred.
     * @param message An additional error message that should be logged.
     * @return A failure result that was caused by the given error.
     */
    public static CommitDiffResult Failure(DiffError error, String message) {
        return new CommitDiffResult(
                Optional.empty(),
                List.of(DiffResult.Failure(error, message).unwrap().getFailure())
        );
    }

    public static CommitDiffResult fromCommitInRepository(final String commitHash, final Repository repository) throws IOException {
        final Git git = repository.getGitRepo().run();
        Assert.assertNotNull(git);
        try (var revWalk = new RevWalk(git.getRepository())) {
            final RevCommit commit = revWalk.parseCommit(ObjectId.fromString(commitHash));
            return new GitDiffer(repository).createCommitDiff(commit);
        }
    }
}
