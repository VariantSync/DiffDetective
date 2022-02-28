package util;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

public record InvocationCounter<A, B>(Function<A, B> inner, AtomicInteger invocationCount) implements Function<A, B> {
    public static <A, B> InvocationCounter<A, B> of(final Function<A, B> inner) {
        return new InvocationCounter<>(inner, new AtomicInteger(0));
    }

    public static <A> InvocationCounter<A, A> justCount() {
        return of(Function.identity());
    }

    @Override
    public B apply(A a) {
        invocationCount.incrementAndGet();
        return inner.apply(a);
    }
}
