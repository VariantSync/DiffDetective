package org.variantsync.diffdetective.variation.diff;

/**
 * Result value for checking the consistency of a VariationDiff (e.g., if it s acyclic).
 */
public class ConsistencyResult {
    private final AssertionError error;

    private ConsistencyResult(final AssertionError error) {
        this.error = error;
    }

    /**
     * Create a result that signals success (i.e, the checked VariationDiff is consistent).
     */
    public static ConsistencyResult Success() {
        return new ConsistencyResult(null);
    }

    /**
     * Create a result that indicates failure with provided reason.
     * @param reason Error explaining why the VariationDiff is inconsistent.
     */
    public static ConsistencyResult Failure(AssertionError reason) {
        return new ConsistencyResult(reason);
    }

    /**
     * Returns true iff this result is a success.
     */
    public boolean isSuccess() {
        return error == null;
    }


    /**
     * Returns true iff this result is a failure.
     */
    public boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Returns null iff this result is a success.
     * Returns an error message if this result is a failure.
     */
    public AssertionError getError() {
        return error;
    }
}
