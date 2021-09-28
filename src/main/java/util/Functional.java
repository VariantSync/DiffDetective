package util;

import java.util.function.Consumer;
import java.util.function.Predicate;

public class Functional {
    public static <T> Consumer<T> when(Predicate<T> condition, Consumer<T> task) {
        return t -> {
            if (condition.test(t)) {
                task.accept(t);
            }
        };
    }
}
