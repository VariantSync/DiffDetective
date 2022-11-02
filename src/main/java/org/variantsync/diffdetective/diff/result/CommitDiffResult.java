package org.variantsync.diffdetective.diff.result;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.CommitDiff;

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
        Logger.debug("{}", message);
        return new CommitDiffResult(
                Optional.empty(),
                List.of(error)
        );
    }
}
