package diff;

import diff.difftree.DiffTree;
import org.pmw.tinylog.Logger;

import java.util.function.Consumer;
import java.util.function.Function;

public class DiffResult<T> {
    private final T result;
    private final DiffError error;

    private DiffResult(T val, DiffError e) {
        result = val;
        error = e;
    }

    public static <T> DiffResult<T> Success(T value) {
        return new DiffResult<>(value, null);
    }

    public static <T> DiffResult<T> Failure(DiffError error) {
        return new DiffResult<>(null, error);
    }

    public static <T> DiffResult<T> Failure(String id) {
        return Failure(new DiffError(id), id);
    }

    public static DiffResult<DiffTree> Failure(Exception e) {
        return Failure(DiffError.from(e), e.getMessage());
    }

    public static <T> DiffResult<T> Failure(DiffError error, String message) {
        Logger.error(message);
        return Failure(error);
    }

    public boolean isSuccess() {
        return result != null;
    }

    public boolean isFailure() {
        return result == null;
    }

    public T getSuccess() {
        return result;
    }

    public DiffError getError() {
        return error;
    }

    public T getOrElse(T other) {
        if (isFailure()) {
            return other;
        }

        return result;
    }

    public void ifSuccess(Consumer<T> then) {
        if (isSuccess()) {
            then.accept(result);
        }
    }

    public void ifFailure(Consumer<DiffError> then) {
        if (isFailure()) {
            then.accept(error);
        }
    }

    public <U> DiffResult<U> map(Function<T, U> f) {
        return DiffResult.Success(f.apply(result));
    }

    public <U> DiffResult<U> flatMap(Function<T, DiffResult<U>> f) {
        return f.apply(result);
    }

    public <U> DiffResult<U> mapFail() {
        return DiffResult.Failure(error);
    }
}
