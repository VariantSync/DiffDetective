package org.variantsync.diffdetective.diff.difftree.filter;

import org.variantsync.functjonal.category.InplaceSemigroup;
import org.variantsync.functjonal.category.SemigroupCannotAppend;

import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An ExplainedFilter records for which reasons elements were filtered.
 * More generally, ExplainedFilter is a predicate that composes other predicates
 * with and semantics. ExplainedFilter keeps track how often each sub-predicate
 * evaluated to false.
 * ExplainedFilter is intended to be used as a filter in streams.
 * @param <T> Domain of the predicate (i.e., type of the values to filter).
 * @author Paul Bittner
 */
public class ExplainedFilter<T> implements Predicate<T> {
    /**
     * Metadata to log how often each filter was applied.
     */
    public static class Explanation {
        /**
         * Explanations form a semigroup (i.e., may be combined).
         * In particular, if two explanations have the same name, their counts are added.
         */
        public final static InplaceSemigroup<Explanation> ISEMIGROUP = (a, b) -> {
            if (a.name.equals(b.name)) {
                a.filterCount += b.filterCount;
            } else {
                throw new SemigroupCannotAppend("Cannot append explanation with different name. Expected \"" + a.name + "\" but was \"" + b.name + "\"!");
            }
        };

        private int filterCount;
        private final String name;

        /**
         * Creates a new explanation with the given description and initial count
         * (i.e., how often a corresponding filter returned true for this reason).
         * @param filterCount Initial hit count.
         * @param name Description for this filter reason.
         */
        public Explanation(int filterCount, String name) {
            this.filterCount = filterCount;
            this.name = name;
        }

        /**
         * Creates a new explanation via {@link Explanation#Explanation(int, String)}
         * with an initial filter count of 0.
         */
        public Explanation(String name) {
            this(0, name);
        }

        /**
         * Copy constructor.
         */
        public Explanation(final Explanation other) {
            this(other.filterCount, other.name);
        }

        /**
         * Resets this explanations filter hit count to 0.
         */
        private void reset() {
            this.filterCount = 0;
        }

        /**
         * Increments this explanations filter count.
         * This means, a filter filtered an element for this explanation's reason.
         */
        public void hit() {
            ++filterCount;
        }

        /**
         * Returns how often a filter returned true for this explanation's reason.
         */
        public int getFilterCount() {
            return filterCount;
        }

        /**
         * Returns the textual description of this filter explanation.
         */
        public String getName() {
            return name;
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

    /**
     * Same as {@link ExplainedFilter#ExplainedFilter(Stream)} but with variadic arguments.
     */
    @SafeVarargs
    public ExplainedFilter(final TaggedPredicate<String, T>... filters) {
        this(Arrays.stream(filters));
    }

    /**
     * Creates an explained filter that always returns true.
     * It will never count any filter hits and thus has no explanations.
     */
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

    /**
     * Returns all sub-filters whose filter hits are recorded.
     * @return
     */
    public List<TaggedPredicate<Explanation, T>> getFilters() {
        return filters;
    }

    /**
     * Returns all possible reasons why this explained filter might return false.
     * @see ExplainedFilter#getFilters
     */
    public Stream<Explanation> getExplanations() {
        return filters.stream().map(TaggedPredicate::tag);
    }
}
