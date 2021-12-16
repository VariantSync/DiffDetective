package diff.difftree.filter;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.analysis.DiffTreeStatistics;
import pattern.atomic.AtomicPatternCatalogue;
import util.TaggedPredicate;

/**
 * A filter on difftrees that is equipped with some metadata T (e.g., for debugging or logging).
 * The condition determines whether a DiffTree should be considered for computation or not.
 * Iff the condition returns true, the DiffTree should be considered.
 */
public final class DiffTreeFilter {
    public static <T> TaggedPredicate<T, DiffTree> Any(final T metadata) {
        return TaggedPredicate.Any(metadata);
    }

    public static TaggedPredicate<String, DiffTree> Any() {
        return Any("any");
    }

    public static TaggedPredicate<String, DiffTree> moreThanTwoAtomicPatternsOf(final AtomicPatternCatalogue patterns) {
        return new TaggedPredicate<>(
                "has more than two atomic patterns",
                tree -> DiffTreeStatistics.getNumberOfUniqueAtomicPatternsIn(tree, patterns) > 1
        );
    }

    public static TaggedPredicate<String, DiffTree> moreThanTwoAtomicPatternsAfterMining() {
        return new TaggedPredicate<>(
                "has more than two atomic patterns",
                tree -> DiffTreeStatistics.getNumberOfUniqueLabelsOfNodes(tree, DiffNode::isCode) > 1
        );
    }

    public static TaggedPredicate<String, DiffTree> notEmpty() {
        return new TaggedPredicate<>(
            "is not empty",
                tree -> !tree.isEmpty()
        );
    }

    public static TaggedPredicate<String, DiffTree> consistent() {
        return new TaggedPredicate<>(
                "is consistent",
                tree -> tree.isConsistent().isSuccess()
        );
    }
}
