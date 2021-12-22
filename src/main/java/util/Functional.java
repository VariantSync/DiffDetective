package util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class Functional {
    public static <T> Consumer<T> when(Predicate<T> condition, Consumer<T> task) {
        return t -> {
            if (condition.test(t)) {
                task.accept(t);
            }
        };
    }

    public static <K1, K2, V1, V2> Map<K2, V2> bimap(
            Map<K1, V1> m,
            Function<K1, K2> key,
            Function<V1, V2> val) {
        return bimap(m, key, val, HashMap::new);
    }

    public static <K1, K2, V1, V2> Map<K2, V2> bimap(
            Map<K1, V1> m,
            Function<K1, K2> key,
            Function<V1, V2> val,
            Supplier<Map<K2, V2>> mapFactory) {
        final Map<K2, V2> result = mapFactory.get();
        for (final Map.Entry<K1, V1> e : m.entrySet()) {
            result.put(key.apply(e.getKey()), val.apply(e.getValue()));
        }
        return result;
    }
}
