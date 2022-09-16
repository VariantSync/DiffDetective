package org.variantsync.diffdetective.diff.difftree.filter;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * An identifiable {@link Predicate} where a predicate is identified with a generic tag.
 * @param <Tag> A tag to identify the predicate.
 * @param <Domain> The type of elements the predicate is defined on.
 * @author Paul Bittner
 */
public record TaggedPredicate<Tag, Domain>(Tag tag, Predicate<Domain> condition) implements Predicate<Domain> {
    @Override
    public boolean test(final Domain element) {
        return condition.test(element);
    }

    /**
     * Logical And for TaggedPredicates.
     * Leaves this and the given predicate untouched.
     * @param other Another predicate that should be conjuncted with this one.
     * @param tagAnd And operator for the tags. Since both tags may have different types, we need a function that composes them.
     *               Typically, this function should be a description of the kind {@code a and b}.
     * @return A predicate that returns true iff both this predicate and the given predicate return true.
     * @param <OtherTag> The type of the other predicate's tag.
     * @param <ResultTag> The type of the resulting predicate's tag.
     * @see Predicate#and
     */
    public <OtherTag, ResultTag> TaggedPredicate<ResultTag, Domain> and(TaggedPredicate<OtherTag, Domain> other, BiFunction<Tag, OtherTag, ResultTag> tagAnd) {
        return new TaggedPredicate<>(tagAnd.apply(this.tag, other.tag), this.condition.and(other.condition));
    }

    /**
     * Specialization of {@link TaggedPredicate#and(TaggedPredicate, BiFunction)} for Strings.
     * The returned predicate's tag is the string composition of both given tags with an {@code "and" in between} and brackets.
     */
    public static <Domain> TaggedPredicate<String, Domain> and(TaggedPredicate<String, Domain> a, TaggedPredicate<String, Domain> b) {
        return a.and(b, (x, y) -> ("(" + x + ") and (" + y + ")"));
    }

    /**
     * Map a function over the predicate's tag.
     * @param f A function that converts this predicate's tag.
     * @return A new predicate with the converted tag.
     * @param <U> The type of the new tag.
     */
    public <U> TaggedPredicate<U, Domain> map(final Function<Tag, U> f) {
        return new TaggedPredicate<>(f.apply(tag), condition);
    }

    /**
     * Creates a tagged predicate with the given tag that always returns {@code true}.
     * @param tag The tag, the created predicate should have.
     * @return A tagged predicate with the given tag that always returns {@code true}.
     * @param <Tag> The type of the tag.
     * @param <Domain> The type of the input of the given predicate (although values of that type are never inspected).
     */
    public static <Tag, Domain> TaggedPredicate<Tag, Domain> Any(final Tag tag) {
        return new TaggedPredicate<>(tag, v -> true);
    }

    /**
     * Specialization of {@link TaggedPredicate#Any(Object)} for Strings.
     * The tag is fixed to {@code "any"}.
     */
    public static <Domain> TaggedPredicate<String, Domain> Any() {
        return Any("any");
    }
}
