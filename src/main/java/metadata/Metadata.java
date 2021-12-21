package metadata;

import java.util.Map;

public interface Metadata {
    Map<String, ?> snapshot();

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
