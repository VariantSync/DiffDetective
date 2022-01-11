package diff.result;

import de.variantsync.functjonal.Product;
import diff.CommitDiff;

import java.util.List;
import java.util.Optional;

public record CommitDiffResult(Product<Optional<CommitDiff>, List<DiffError>> unwrap) {
    public static CommitDiffResult Failure(DiffError error, String message) {
        return new CommitDiffResult(new Product<>(
                Optional.empty(),
                List.of(DiffResult.Failure(error, message).unwrap().getFailure()))
        );
    }
}
