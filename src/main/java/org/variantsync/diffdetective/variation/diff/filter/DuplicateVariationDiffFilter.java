package org.variantsync.diffdetective.variation.diff.filter;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Filters all duplicates in a list of VariationDiffs regarding isomorphism.
 * @author Paul Bittner
 */
public class DuplicateVariationDiffFilter<L extends Label> {
    private final BiFunction<VariationDiff<L>, VariationDiff<L>, Boolean> equality;

    /**
     * Creates a new duplication filter that uses the given predicate to determine equality of VariationDiffs.
     * @param equalityCondition Predicate that determines equality of VariationDiffs.
     */
    public DuplicateVariationDiffFilter(final BiFunction<VariationDiff<L>, VariationDiff<L>, Boolean> equalityCondition) {
        this.equality = equalityCondition;
    }

    /**
     * Filters the given list by removing all duplicates according this filter's equality function.
     * @param treesWithDuplicates List of DiffTress that may contain duplicate trees.
     * @return A list without duplicates. Every tree in the returned list was contained in the input list.
     */
    public List<VariationDiff<L>> filterDuplicates(final List<VariationDiff<L>> treesWithDuplicates) {
        final List<VariationDiff<L>> distinct = new ArrayList<>(treesWithDuplicates.size());

        for (final VariationDiff<L> candidate : treesWithDuplicates) {
            if (distinct.stream().noneMatch(t -> equality.apply(candidate, t))) {
                distinct.add(candidate);
            }
        }

        return distinct;
    }
}
