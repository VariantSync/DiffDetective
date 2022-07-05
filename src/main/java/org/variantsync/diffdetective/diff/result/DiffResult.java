package org.variantsync.diffdetective.diff.result;

import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;
import org.variantsync.functjonal.Result;

import java.util.function.Function;

/**
 * The result of processing a diff.
 * The result either contains the desired result or an error message in case an error occurred.
 * @param <T> The type of the result upon success.
 */
public class DiffResult<T> {
    private final Result<T, DiffError> result;

    private DiffResult(Result<T, DiffError> result) {
        this.result = result;
    }

    /**
     * Get the internal representation of this result.
     * @see org.variantsync.functjonal.Result
     */
    public Result<T, DiffError> unwrap() {
        return result;
    }

    /**
     * DiffResult is a functor.
     * Applies the given function to this results value if it has one.
     * @param f Function to apply to this result's value.
     * @return A new result with the transformed value.
     * @param <U> Type to which the value of this result is transformed to by the given function.
     */
    public <U> DiffResult<U> map(final Function<T, U> f) {
        return new DiffResult<>(result.map(f));
    }

    /// Constructors

    /**
     * Create a result that indicates success.
     * The result will hold the given value and no error message.
     * @param val The result value.
     * @return A success result.
     * @param <T> The type of the return value.
     */
    public static <T> DiffResult<T> Success(T val) {
        return new DiffResult<>(Result.Success(val));
    }

    /**
     * Create a result that indicates failure.
     * The result will hold an error message and no value.
     * @param error The error that occurred.
     * @return A failure result.
     * @param <T> The type the result value should have had if no error had been occurred.
     */
    public static <T> DiffResult<T> Failure(DiffError error) {
        return Failure(error, error.id());
    }

    /**
     * Convenience constructor to create a result from an {@link IllFormedAnnotationException}.
     * The produced result will indicate failure with the given error message.
     * @param e The exception that was thrown when processing a diff.
     * @return A failure result.
     * @param <T> The type the result value should have had if no error had been occurred.
     */
    public static <T> DiffResult<T> Failure(IllFormedAnnotationException e) {
        return Failure(e.getType(), e.getMessage());
    }


    /**
     * Create a result that indicates failure.
     * The result will hold the given DiffError and no value.
     * The message will be logged and then discarded.
     * @param error The error that occurred.
     * @param message An additional error message that should be logged.
     * @return A failure result.
     * @param <T> The type the result value should have had if no error had been occurred.
     */
    public static <T> DiffResult<T> Failure(DiffError error, String message) {
        Logger.debug("[DiffResult::Failure] {}", message);
        return new DiffResult<>(Result.Failure(error));
    }
}
