package util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record TaggedPredicate<Tag, Domain>(Tag tag, Predicate<Domain> condition) {
    public boolean test(final Domain element) {
        return condition.test(element);
    }

    public TaggedPredicate<Tag, Domain> and(TaggedPredicate<Tag, Domain> other, BiFunction<Tag, Tag, Tag> tagAnd) {
        return new TaggedPredicate<>(tagAnd.apply(this.tag, other.tag), this.condition.and(other.condition));
    }

    public TaggedPredicate<String, Domain> and(TaggedPredicate<String, Domain> other) {
        return new TaggedPredicate<>("(" + this.tag + ") and (" + other.tag + ")", this.condition.and(other.condition));
    }

    public <U> TaggedPredicate<U, Domain> map(final Function<Tag, U> f) {
        return new TaggedPredicate<>(f.apply(tag), condition);
    }

    public static <Tag, Domain> TaggedPredicate<Tag, Domain> Any(final Tag tag) {
        return new TaggedPredicate<>(tag, v -> true);
    }

    public static <Domain> TaggedPredicate<String, Domain> Any() {
        return Any("any");
    }
}
