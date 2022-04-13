package org.variantsync.diffdetective.diff.difftree.filter;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import static org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns.*;

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

    public static TaggedPredicate<String, DiffTree> moreThanOneCodeNode() {
        return new TaggedPredicate<>(
                "has more than one elementary pattern",
                tree -> tree.count(DiffNode::isCode) > 1
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

    public static TaggedPredicate<String, DiffTree> hasAtLeastOneEditToVariability() {
        return new TaggedPredicate<>(
                "has edits to variability",
                tree -> tree.anyMatch(n ->
                        n.isCode() && !AddToPC.matches(n) && !RemFromPC.matches(n) && !Untouched.matches(n)
                )
        );
    }
}
