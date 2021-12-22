package diff.difftree.filter;

import util.functional.Semigroup;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ExplainedFilter is intended to be used as a filter in streams.
 * While filtering it records for which reasons elements were filtered.
 * More generally, ExplainedFilter is a predicate that composes other predicates
 * with and semantics. ExplainedFilter keeps track how often each sub-predicate
 * evaluated to false.
 * @param <T>
 */
public class ExplainedFilter<T> implements Predicate<T> {
    /**
     * Metadata to log how often each filter was applied.
     */
    public static class Explanation implements Semigroup<Explanation> {
        private int filterCount;
        private final String name;

        public Explanation(int filterCount, String name) {
            this.filterCount = filterCount;
            this.name = name;
        }

        public Explanation(String name) {
            this(0, name);
        }

        public Explanation(final Explanation other) {
            this(other.filterCount, other.name);
        }

        private void reset() {
            this.filterCount = 0;
        }

        public void hit() {
            ++filterCount;
        }

        public int getFilterCount() {
            return filterCount;
        }

        public String getName() {
            return name;
        }

        @Override
        public void append(Explanation other) {
            if (other.name.equals(this.name)) {
                this.filterCount += other.filterCount;
            } else {
                throw new UnsupportedOperationException("Cannot append explanation with different name. Expected \"" + this.name + "\" but was \"" + other.name + "\"!");
            }
        }
    }

    private final List<TaggedPredicate<Explanation, T>> filters;

    /**
     * Creates an ExplainedFilter by conjunction of all given filters.
     * @param namedFilters Filters to compose. Each filter has to be explained by a (unique) name.
     */
    public ExplainedFilter(final Stream<TaggedPredicate<String, T>> namedFilters) {
        this.filters = namedFilters.map(
                filter -> filter.map(Explanation::new)
        ).collect(Collectors.toList());
    }

    @SafeVarargs
    public ExplainedFilter(final TaggedPredicate<String, T>... filters) {
        this(Arrays.stream(filters));
    }

    public static <A> ExplainedFilter<A> Any() {
        return new ExplainedFilter<>(Stream.empty());
    }

    @Override
    public boolean test(final T element) {
        for (final TaggedPredicate<Explanation, T> filter : filters) {
            if (!filter.condition().test(element)) {
                filter.tag().hit();
                return false;
            }
        }

        return true;
    }

    /**
     * Resets all explanations such that the filter can be reused as if it was
     * just created.
     */
    public void resetExplanations() {
        for (final var filter : filters) {
            filter.tag().reset();
        }
    }

    public List<TaggedPredicate<Explanation, T>> getFilters() {
        return filters;
    }

    public Stream<Explanation> getExplanations() {
        return filters.stream().map(TaggedPredicate::tag);
    }
}
