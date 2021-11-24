package diff.difftree.filter;

import diff.difftree.DiffTree;

import java.util.function.Function;
import java.util.function.Predicate;

/**
 * A filter on difftrees that is equipped with some metadata T (e.g., for debugging or logging).
 * The condition determines whether a DiffTree should be considered for computation or not.
 * Iff the condition returns true, the DiffTree should be considered.
 */
public record DiffTreeFilter<T>(T metadata, Predicate<DiffTree> condition) {
    public <U> DiffTreeFilter<U> map(final Function<T, U> f) {
        return new DiffTreeFilter<>(f.apply(metadata), condition);
    }
}
