package org.variantsync.diffdetective.diff.difftree;

public class ConsistencyResult {
    private final AssertionError error;

    private ConsistencyResult(final AssertionError error) {
        this.error = error;
    }

    public static ConsistencyResult Success() {
        return new ConsistencyResult(null);
    }

    public static ConsistencyResult Failure(AssertionError failure) {
        return new ConsistencyResult(failure);
    }

    public boolean isSuccess() {
        return error == null;
    }

    public boolean isFailure() {
        return !isSuccess();
    }

    public AssertionError getError() {
        return error;
    }
}
