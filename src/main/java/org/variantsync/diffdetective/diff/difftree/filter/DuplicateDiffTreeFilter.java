package org.variantsync.diffdetective.diff.difftree.filter;

import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Filters all duplicates in a list of DiffTrees regarding isomorphism.
 * @author Paul Bittner
 */
public class DuplicateDiffTreeFilter {
    private final BiFunction<DiffTree, DiffTree, Boolean> equality;

    /**
     * Creates a new duplication filter that uses the given predicate to determine equality of DiffTrees.
     * @param equalityCondition Predicate that determines equality of DiffTrees.
     */
    public DuplicateDiffTreeFilter(final BiFunction<DiffTree, DiffTree, Boolean> equalityCondition) {
        this.equality = equalityCondition;
    }

    /**
     * Filters the given list by removing all duplicates according this filter's equality function.
     * @param treesWithDuplicates List of DiffTress that may contain duplicate trees.
     * @return A list without duplicates. Every tree in the returned list was contained in the input list.
     */
    public List<DiffTree> filterDuplicates(final List<DiffTree> treesWithDuplicates) {
        final List<DiffTree> distinct = new ArrayList<>(treesWithDuplicates.size());

        for (final DiffTree candidate : treesWithDuplicates) {
            if (distinct.stream().noneMatch(t -> equality.apply(candidate, t))) {
                distinct.add(candidate);
            }
        }

        return distinct;
    }
}
