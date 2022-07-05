package org.variantsync.diffdetective.diff.difftree;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A value that refers to the state before or after an edit occurred.
 * These correspond to the time values b and a from our paper.
 * @author Paul Bittner
 */
public enum Time {
    BEFORE, AFTER;

    /**
     * Invoke the given function for each time value (i.e., each value in this enum).
     * @param f callback
     */
    public static void forall(final Consumer<Time> f) {
        f.accept(BEFORE);
        f.accept(AFTER);
    }

    /**
     * Pattern matching on Time for Suppliers.
     * Invokes the corresponding supplier depending on this time's value.
     * If this time is BEFORE, then the before supplier is invoked, otherwise the after supplier is invoked.
     *
     * @param before The supplier to invoke when this time is BEFORE.
     * @param after The supplier to invoke when this time is AFTER.
     * @return The value returned by the corresponding supplier.
     * @param <T> Type of the value to produce.
     */
    public <T> T match(final Supplier<T> before, final Supplier<T> after) {
        return this.<Supplier<T>>match(before, after).get();
    }

    /**
     * Pattern matching on Time.
     * Returns the corresponding value depending on this time's value.
     *
     * @param before Value to return if this time is BEFORE.
     * @param after Value to return if this time is AFTER.
     * @return The value for the corresponding time.
     * @param <T> Value type
     */
    public <T> T match(final T before, final T after) {
        return switch (this) {
            case BEFORE -> before;
            case AFTER -> after;
        };
    }
}
