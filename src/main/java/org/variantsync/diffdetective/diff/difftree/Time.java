package org.variantsync.diffdetective.diff.difftree;

import java.util.function.Consumer;
import java.util.function.Supplier;

public enum Time {
    BEFORE, AFTER;

    public static void forall(final Consumer<Time> f) {
        f.accept(BEFORE);
        f.accept(AFTER);
    }

    public <T> T match(final Supplier<T> before, final Supplier<T> after) {
        return this.<Supplier<T>>match(before, after).get();
    }

    public <T> T match(final T before, final T after) {
        return switch (this) {
            case BEFORE -> before;
            case AFTER -> after;
        };
    }
}
