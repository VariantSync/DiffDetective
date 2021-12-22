package diff;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public record DiffResultWithErrors<T>(Optional<T> result, List<DiffError> errors) {
    public static <U> DiffResultWithErrors<U> Failure(DiffError e) {
        return new DiffResultWithErrors<>(Optional.empty(), List.of(e));
    }

    public static <U> DiffResultWithErrors<U> of(DiffResult<U> r) {
        if (r.isSuccess()) {
            return new DiffResultWithErrors<>(Optional.of(r.getSuccess()), new ArrayList<>());
        } else {
            return Failure(r.getError());
        }
    }
}
