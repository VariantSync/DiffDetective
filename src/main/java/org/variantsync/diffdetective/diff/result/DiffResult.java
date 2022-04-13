package org.variantsync.diffdetective.diff.result;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;
import org.variantsync.functjonal.Result;

import java.util.function.Function;

public class DiffResult<T> {
    private final Result<T, DiffError> result;

    private DiffResult(Result<T, DiffError> result) {
        this.result = result;
    }

    public Result<T, DiffError> unwrap() {
        return result;
    }

    public <U> DiffResult<U> map(final Function<T, U> f) {
        return new DiffResult<>(result.map(f));
    }

    /// Constructors

    public static <T> DiffResult<T> Success(T val) {
        return new DiffResult<>(Result.Success(val));
    }

    public static <T> DiffResult<T> Failure(DiffError error) {
        return Failure(error, error.id());
    }

    public static <T> DiffResult<T> Failure(IllFormedAnnotationException e) {
        return Failure(e.getType(), e.getMessage());
    }

    public static <T> DiffResult<T> Failure(DiffError error, String message) {
        Logger.debug("[DiffResult::Failure] " + message);
        return new DiffResult<>(Result.Failure(error));
    }
}
