package util;

import java.util.function.Supplier;

public class Assert {
    public static void assertTrue(boolean cond) {
        assertTrue(cond, "assertion failed");
    }

    public static void assertTrue(boolean cond, final Supplier<String> errorMessage) {
        if (!cond) {
            throw new AssertionError(errorMessage.get());
        }
    }

    public static void assertTrue(boolean cond, String errorMessage) {
        if (!cond) {
            throw new AssertionError(errorMessage);
        }
    }

    public static void assertNonNull(Object o) {
        if (o == null) {
            throw new AssertionError("Given object is null but assumed to be not null.");
        }
    }
}
