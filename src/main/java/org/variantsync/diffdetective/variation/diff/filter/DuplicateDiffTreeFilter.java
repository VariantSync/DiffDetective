package org.variantsync.diffdetective.variation.diff.filter;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Filters all duplicates in a list of DiffTrees regarding isomorphism.
 * @author Paul Bittner
 */
public class DuplicateDiffTreeFilter<L extends Label> {
    private final BiFunction<DiffTree<L>, DiffTree<L>, Boolean> equality;

    /**
     * Creates a new duplication filter that uses the given predicate to determine equality of DiffTrees.
     * @param equalityCondition Predicate that determines equality of DiffTrees.
     */
    public DuplicateDiffTreeFilter(final BiFunction<DiffTree<L>, DiffTree<L>, Boolean> equalityCondition) {
        this.equality = equalityCondition;
    }

    /**
     * Filters the given list by removing all duplicates according this filter's equality function.
     * @param treesWithDuplicates List of DiffTress that may contain duplicate trees.
     * @return A list without duplicates. Every tree in the returned list was contained in the input list.
     */
    public List<DiffTree<L>> filterDuplicates(final List<DiffTree<L>> treesWithDuplicates) {
        final List<DiffTree<L>> distinct = new ArrayList<>(treesWithDuplicates.size());

        for (final DiffTree<L> candidate : treesWithDuplicates) {
            if (distinct.stream().noneMatch(t -> equality.apply(candidate, t))) {
                distinct.add(candidate);
            }
        }

        return distinct;
    }
}
