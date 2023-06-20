package org.variantsync.diffdetective.variation.diff.filter;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

import static org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses.*;

/**
 * A filter on VariationDiffs that is equipped with some metadata T (e.g., for debugging or logging).
 * The condition determines whether a VariationDiff should be considered for computation or not.
 * Iff the condition returns true, the VariationDiff should be considered.
 * @author Paul Bittner
 */
public final class VariationDiffFilter<L extends Label> {
    /**
     * Returns a tagged predicate that always returns true and is tagged with the given metadata.
     */
    public static <T, L extends Label> TaggedPredicate<T, VariationDiff<? extends L>> Any(final T metadata) {
        return TaggedPredicate.Any(metadata);
    }

    /**
     * Returns a tagged predicate that always returns true and is tagged with the String {@code "any"}.
     */
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> Any() {
        return Any("any");
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the VariationDiff has more than one artifact node ({@link DiffNode#isArtifact()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> moreThanOneArtifactNode() {
        return new TaggedPredicate<>(
                "has more than one artifact node",
                tree -> tree.count(DiffNode::isArtifact) > 1
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the VariationDiff is not empty ({@link VariationDiff#isEmpty()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> notEmpty() {
        return new TaggedPredicate<>(
            "is not empty",
                tree -> !tree.isEmpty()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the VariationDiff is {@link VariationDiff#isConsistent() consistent}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> consistent() {
        return new TaggedPredicate<>(
                "is consistent",
                tree -> tree.isConsistent().isSuccess()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the VariationDiff has at least one artifact node ({@link DiffNode#isArtifact()})
     * that does not match any edit class of
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#AddToPC},
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#RemFromPC},
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#Untouched}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static <L extends Label> TaggedPredicate<String, VariationDiff<? extends L>> hasAtLeastOneEditToVariability() {
        return new TaggedPredicate<>(
                "has edits to variability",
                tree -> tree.anyMatch(n ->
                        n.isArtifact() && !AddToPC.matches(n) && !RemFromPC.matches(n) && !Untouched.matches(n)
                )
        );
    }
}
