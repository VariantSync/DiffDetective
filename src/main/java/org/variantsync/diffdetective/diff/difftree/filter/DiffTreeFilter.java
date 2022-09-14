package org.variantsync.diffdetective.diff.difftree.filter;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import static org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses.*;

/**
 * A filter on DiffTrees that is equipped with some metadata T (e.g., for debugging or logging).
 * The condition determines whether a DiffTree should be considered for computation or not.
 * Iff the condition returns true, the DiffTree should be considered.
 * @author Paul Bittner
 */
public final class DiffTreeFilter {
    /**
     * Returns a tagged predicate that always returns true and is tagged with the given metadata.
     */
    public static <T> TaggedPredicate<T, DiffTree> Any(final T metadata) {
        return TaggedPredicate.Any(metadata);
    }

    /**
     * Returns a tagged predicate that always returns true and is tagged with the String {@code "any"}.
     */
    public static TaggedPredicate<String, DiffTree> Any() {
        return Any("any");
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree has more than one artifact node ({@link DiffNode#isArtifact()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> moreThanOneArtifactNode() {
        return new TaggedPredicate<>(
                "has more than one artifact node",
                tree -> tree.count(DiffNode::isArtifact) > 1
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree is not empty ({@link DiffTree#isEmpty()}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> notEmpty() {
        return new TaggedPredicate<>(
            "is not empty",
                tree -> !tree.isEmpty()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree is {@link DiffTree#isConsistent() consistent}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> consistent() {
        return new TaggedPredicate<>(
                "is consistent",
                tree -> tree.isConsistent().isSuccess()
        );
    }

    /**
     * Returns a tagged predicate that returns true iff
     * the DiffTree has at least one artifact node ({@link DiffNode#isArtifact()})
     * that does not match any edit class of
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#AddToPC},
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#RemFromPC},
     * {@link org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses#Untouched}.
     * The predicate is tagged with a String description of the predicate.
     */
    public static TaggedPredicate<String, DiffTree> hasAtLeastOneEditToVariability() {
        return new TaggedPredicate<>(
                "has edits to variability",
                tree -> tree.anyMatch(n ->
                        n.isArtifact() && !AddToPC.matches(n) && !RemFromPC.matches(n) && !Untouched.matches(n)
                )
        );
    }
}
