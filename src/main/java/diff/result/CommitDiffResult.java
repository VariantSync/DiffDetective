package diff.result;

import diff.CommitDiff;

import java.util.List;
import java.util.Optional;

public record CommitDiffResult(Optional<CommitDiff> diff, List<DiffError> errors) {
    public static CommitDiffResult Failure(DiffError error, String message) {
        return new CommitDiffResult(
                Optional.empty(),
                List.of(DiffResult.Failure(error, message).unwrap().getFailure())
        );
    }
}
