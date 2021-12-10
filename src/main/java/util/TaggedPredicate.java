package util;

import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;

public record TaggedPredicate<Tag, Domain>(Tag tag, Predicate<Domain> condition) {
    public boolean test(final Domain element) {
        return condition.test(element);
    }

    public <OtherTag, ResultTag> TaggedPredicate<ResultTag, Domain> and(TaggedPredicate<OtherTag, Domain> other, BiFunction<Tag, OtherTag, ResultTag> tagAnd) {
        return new TaggedPredicate<>(tagAnd.apply(this.tag, other.tag), this.condition.and(other.condition));
    }

    public static <Domain> TaggedPredicate<String, Domain> and(TaggedPredicate<String, Domain> a, TaggedPredicate<String, Domain> b) {
        return a.and(b, (x, y) -> ("(" + x + ") and (" + y + ")"));
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
