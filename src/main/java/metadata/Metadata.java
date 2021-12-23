package metadata;

import util.semigroup.Semigroup;

import java.util.LinkedHashMap;
import java.util.Map;

public interface Metadata<T> extends Semigroup<T> {
    /**
     * Create a key-value store of the metadata that can be used for serialization.
     * @return A LinkedHashMap that stores all relevant properties to export.
     *         The return type has to be a LinkedHashMap to obtain insertion-order iteration.
     */
    LinkedHashMap<String, ?> snapshot();

    static String show(final Map<String, Object> properties) {
        StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, Object> property : properties.entrySet()) {
            result.append(show(property.getKey(), property.getValue()));
        }
        return result.toString();
    }

    static <T> String show(final String name, T value) {
        return name + ": " + value.toString() + "\n";
    }
}
