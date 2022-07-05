package org.variantsync.diffdetective.util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * Counter for the number of times a wrapped function is called.
 *
 * <p>This class is thread safe.
 *
 * @param inner the wrapped function
 * @param invocationCount the number of times {@code inner} was called
 */
public record InvocationCounter<A, B>(Function<A, B> inner, AtomicInteger invocationCount) implements Function<A, B> {
    /** Count the number of invocations of {@code inner} by wrapping it. */
    public static <A, B> InvocationCounter<A, B> of(final Function<A, B> inner) {
        return new InvocationCounter<>(inner, new AtomicInteger(0));
    }

    /** Count the number of invocations of the identity function. */
    public static <A> InvocationCounter<A, A> justCount() {
        return of(Function.identity());
    }

    @Override
    public B apply(A a) {
        invocationCount.incrementAndGet();
        return inner.apply(a);
    }
}
