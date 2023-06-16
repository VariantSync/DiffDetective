package org.variantsync.diffdetective.variation.diff;

import org.apache.commons.lang3.function.FailableConsumer;

import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Type of change made to an artifact (e.g., a line of text in a text-based diff).
 * An artifact is either added, removed, or unchanged.
 * These values correspond to the domain of the Delta function from our paper.
 */
public enum DiffType {
    ADD("+"),
    REM("-"),
    NON(" ");

    public final String symbol;

    DiffType(String symbol) {
        this.symbol = symbol;
    }

    /**
     * Returns the diff type for which corresponding artifacts exist only at the given time.
     * REMoved artifacts exist only BEFORE the edit.
     * ADDed artifacts exist only AFTER the edit.
     */
    public static DiffType thatExistsOnlyAt(Time time) {
        return switch (time) {
            case BEFORE -> REM;
            case AFTER -> ADD;
        };
    }

    /**
     * Returns the diff type for which corresponding artifacts exist at all times in the given set.
     * {@link #NON Unchanged} artifacts exist at all times.
     * REMoved artifacts exist only BEFORE the edit.
     * ADDed artifacts exist only AFTER the edit.
     * @return {@link Optional#empty()} if the given set is empty.
     *         Otherwise returns non-empty optional with the DiffType that exists
     *         exactly at all times in the given set.
     */
    public static Optional<DiffType> thatExistsOnlyAtAll(final Set<Time> t) {
        if (t.isEmpty()) {
            return Optional.empty();
        }

        final boolean b = t.contains(Time.BEFORE);
        final boolean a = t.contains(Time.AFTER);
        final DiffType d;
        if (b && a) {
            d = DiffType.NON;
        } else if (b) {
            d = DiffType.thatExistsOnlyAt(Time.BEFORE);
        } else {
            d = DiffType.thatExistsOnlyAt(Time.AFTER);
        }
        return Optional.of(d);
    }

    /**
     * Runs the given procedure depending whether this diff types exists at the respective times.
     * Runs the first given procedure if the edited artifact existed before the edit (DiffType != ADD).
     * Runs the second given procedure if the edited artifact exists after the edit (DiffType != REM).
     * Note: Runs both procedures sequentially if the artifact was not edited and thus
     *       exists before and after the edit (DiffType = NON).
     * @param ifExistsBefore Procedure to run if the edited artifact existed before the edit (DiffType != ADD).
     * @param ifExistsAfter Procedure to run if the edited artifact exists after the edit (DiffType != REM).
     */
    public void forAllTimesOfExistence(final Runnable ifExistsBefore, final Runnable ifExistsAfter) {
        if (this != DiffType.ADD) {
            ifExistsBefore.run();
        }
        if (this != DiffType.REM) {
            ifExistsAfter.run();
        }
    }

    /**
     * Runs the given procedure for any time at which elements with this diff type exist.
     * The consumer will be invoked at least once and at most twice.
     * @param t Procedure to run for each time elements with this diff type exist.
     */
    public void forAllTimesOfExistence(final Consumer<Time> t) {
        forAllTimesOfExistence(
                () -> t.accept(Time.BEFORE),
                () -> t.accept(Time.AFTER)
        );
    }

    /**
     * Runs the given task once for each argument that would exist at a certain time if it had this diff type.
     * Runs task on ifExistsBefore if the value existed before the edit (DiffType != ADD).
     * Runs task on ifExistsAfter if the value exists after the edit (DiffType != ADD).
     * Note: Runs task on both arguments sequentially if the artifact was not edited (DiffType == NON).
     * Some tasks may not be run if a task throws an exception.
     *
     * @param ifExistsBefore Argument that is valid if the diff did not add.
     * @param ifExistsAfter Argument that is valid if the edit did not remove.
     * @param task Task to run with all given arguments that are valid w.r.t. to this DiffType's lifetime.
     * @throws E iff {@code task} throws {@code E}
     */
    public <T, E extends Throwable> void forAllTimesOfExistence(
        final T ifExistsBefore,
        final T ifExistsAfter,
        final FailableConsumer<T, E> task
    ) throws E {
        if (this != DiffType.ADD) {
            task.accept(ifExistsBefore);
        }
        if (this != DiffType.REM) {
            task.accept(ifExistsAfter);
        }
    }

    /**
     * Returns the inverse diff type regarding time.
     * Addition becomes removal,
     * removal becomes addition,
     * and unchanged does not have an inverse.
     */
    public DiffType inverse() {
        return switch (this) {
            case ADD -> REM;
            case REM -> ADD;
            // Should this probably just be NON?
            case NON -> throw new AssertionError("DiffType NON does not have an inverse!");
        };
    }

    /**
     * Parses the diff type from a line taken from a text-based diff.
     * @param line A line in a patch.
     * @return The type of edit of <code>line</code> or null if the line is empty.
     */
    public static DiffType ofDiffLine(String line) {
        if (line.startsWith(ADD.symbol)) {
            return ADD;
        } else if (line.startsWith(REM.symbol)) {
            return REM;
        } else {
            return NON;
        }
    }

    /**
     * Creates a DiffType from its value names.
     * @see Enum#name()
     * @param name a string starting with one of ADD, REM, or NON
     * @return The DiffType that has the given name
     */
    public static DiffType fromName(final String name) {
        if (name.startsWith(ADD.name())) {
            return ADD;
        } else if (name.startsWith(REM.name())) {
            return REM;
        } else {
            return NON;
        }
    }

    /**
     * Returns true iff artifacts with this diff type exist before the edit.
     */
    public boolean existsBefore() {
        return existsAtTime(Time.BEFORE);
    }

    /**
     * Returns true iff artifacts with this diff type exist after the edit.
     */
    public boolean existsAfter() {
        return existsAtTime(Time.AFTER);
    }

    /**
     * Returns true iff artifacts with this diff type exist at the given time.
     * See the respective equation in our paper next to Definition 3.1.
     */
    public boolean existsAtTime(Time time) {
        return (time == Time.BEFORE && this != ADD) || (time == Time.AFTER && this != REM);
    }

    /**
     * Returns the number of bits required for storing {@link ordinal}.
     */
    public static int getRequiredBitCount() {
        return 3;
    }
}
