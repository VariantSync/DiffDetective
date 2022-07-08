package org.variantsync.diffdetective.util;

import java.util.function.Supplier;

/**
 * Assertions which cannot be disabled.
 *
 * <p>Assertions should document preconditions and postconditions which aren't enforced by the type
 * system. All assertions in this class throw {@code AssertionError} to abort the program
 * execution. At no point an {@code AssertionError} should be catched and the normal control flow
 * resumed. The only valid reasons to catch such an exception is to add additional information to
 * the exception or to ensure a fast and correct program shutdown.
 *
 * <p>The default Java assertions are disabled by default which makes them unsuitable for this
 * research project because correctness is a higher goal then a little performance. This class
 * provides assertions in a similar manner as JUnit and should be used for all assertions outside
 * of unit tests (for unit tests use JUnit's assertions). Disabling or Enabling Java's
 * {@code assert} should make no difference whatsoever.
 */
public class Assert {
    /**
     * Abort program execution if {@code cond} is false.
     *
     * <p>If the checked condition is not obvious in the source code
     * {@link assertTrue(boolean, String)} should be used to help identifying issues quickly.
     *
     * @param cond the condition which has to be true
     * @throws AssertionError if {@code cond} is false
     */
    public static void assertTrue(boolean cond) {
        assertTrue(cond, "assertion failed");
    }

    /**
     * Abort program execution if {@code cond} is false.
     *
     * <p>Overload of {@link assertTrue(boolean, String)} for computationally expensive messages. It's
     * used to save the little execution time to construct a helpful error message in the common
     * case of a correct assumption.
     *
     * @param cond the condition which has to be true
     * @param errorMessage a supplier of a single message identifying what condition is checked
     * @throws AssertionError if {@code cond} is false
     */
    public static void assertTrue(boolean cond, final Supplier<String> errorMessage) {
        if (!cond) {
            fail(errorMessage.get());
        }
    }

    /**
     * Abort program execution if {@code cond} is false.
     *
     * <p>If {@code errorMessage} is computationally expensive, consider using
     * {@link assertTrue(boolean, Supplier<String>)}.
     *
     * @param cond the condition which has to be true
     * @param errorMessage a message identifying what condition is checked
     * @throws AssertionError if {@code cond} is false
     */
    public static void assertTrue(boolean cond, String errorMessage) {
        if (!cond) {
            fail(errorMessage);
        }
    }

    /** Throws {@link AssertionError} with {@code errorMessage} as error message. */
    public static void fail(String errorMessage) {
        throw new AssertionError(errorMessage);
    }

    /** Abort program execution if {@code o} is {@code null}. */
    public static void assertNotNull(Object o) {
        if (o == null) {
            fail("Given object is null but assumed to be not null.");
        }
    }
}
