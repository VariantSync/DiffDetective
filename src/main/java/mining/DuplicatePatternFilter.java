package mining;

import diff.difftree.DiffTree;
import diff.difftree.analysis.Equality;

import java.util.ArrayList;
import java.util.List;

/**
 * Filters all duplicates in a list of DiffTrees regarding isomorphism.
 */
public class DuplicatePatternFilter {
    public static List<DiffTree> filterDuplicates(final List<DiffTree> treesWithDuplicates) {
        final List<DiffTree> distinct = new ArrayList<>(treesWithDuplicates.size());

        for (final DiffTree candidate : treesWithDuplicates) {
            if (distinct.stream().noneMatch(t -> Equality.isomorph(candidate, t))) {
                distinct.add(candidate);
            }
        }

        return distinct;
    }
}
