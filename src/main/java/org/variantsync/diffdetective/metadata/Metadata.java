package org.variantsync.diffdetective.metadata;

import org.tinylog.Logger;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.category.InplaceSemigroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Metadata<T> {
    /**
     * Create a key-value store of the metadata that can be used for serialization.
     * @return A LinkedHashMap that stores all relevant properties to export.
     *         The return type has to be a LinkedHashMap to obtain insertion-order iteration.
     */
    LinkedHashMap<String, ?> snapshot();

    /**
     * Metadata should be composable.
     * Composition should be inplace to optimize performance.
     */
    InplaceSemigroup<T> semigroup();

    default void append(T other) {
        semigroup().appendToFirst(Cast.unchecked(this), other);
    }

    static String show(final Map<String, ?> properties) {
        StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, ?> property : properties.entrySet()) {
            result.append(show(property.getKey(), property.getValue()));
        }
        return result.toString();
    }

    static <T> String show(final String name, T value) {
        return name + ": " + value.toString() + "\n";
    }

    default String exportTo(final Path file) {
        try {
            final String result = show(snapshot());
            IO.write(file, result);
            return result;
        } catch (IOException e) {
            Logger.error(e);
            System.exit(0);
            return "";
        }
    }
}
