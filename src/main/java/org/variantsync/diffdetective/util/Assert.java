package org.variantsync.diffdetective.util;

import java.util.function.Supplier;

public class Assert {
    public static void assertTrue(boolean cond) {
        assertTrue(cond, "assertion failed");
    }

    public static void assertTrue(boolean cond, final Supplier<String> errorMessage) {
        if (!cond) {
            fail(errorMessage.get());
        }
    }

    public static void assertTrue(boolean cond, String errorMessage) {
        if (!cond) {
            fail(errorMessage);
        }
    }

    public static void fail(String errorMessage) {
        throw new AssertionError(errorMessage);
    }

    public static void assertNotNull(Object o) {
        if (o == null) {
            fail("Given object is null but assumed to be not null.");
        }
    }
}
