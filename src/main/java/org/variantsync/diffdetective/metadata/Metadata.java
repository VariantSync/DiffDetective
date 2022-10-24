package org.variantsync.diffdetective.metadata;

import org.tinylog.Logger;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.category.InplaceSemigroup;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Generic interface to model composable and printable metadata.
 * @param <T> The type of metadata. Should be the subclasses type.
 */
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

    /**
     * Append the other metadata's values to this metadata.
     * The default implementation uses the {@link #semigroup()} for this purpose.
     * @param other The metadata to append to this metadata. Remains unchanged (if the semigroup leaves it unchanged).
     */
    default void append(T other) {
        semigroup().appendToFirst(Cast.unchecked(this), other);
    }

    /**
     * Prints all key-value pairs to a single string.
     * Falls back to {@link #show(String, Object)} on each entry.
     * @param properties The key-value store to print.
     * @return A string showing all key-value pairs.
     */
    static String show(final Map<String, ?> properties) {
        StringBuilder result = new StringBuilder();
        for (final Map.Entry<String, ?> property : properties.entrySet()) {
            result.append(show(property.getKey(), property.getValue()));
        }
        return result.toString();
    }

    /**
     * Prints the given key, value pair to text.
     * @param name Name of the metadata entry.
     * @param value Value of the metadata entry.
     * @return A String <code>name: value\n</code>.
     * @param <T> The type of the metadata value.
     */
    static <T> String show(final String name, T value) {
        return name + ": " + value.toString() + "\n";
    }

    /**
     * Export this metadata to the given file.
     * @param file File to write.
     * @return The exported file's content.
     * @see IO#write
     */
    default String exportTo(final Path file) {
        try {
            final String result = show(snapshot());
            IO.write(file, result);
            return result;
        } catch (IOException e) {
            Logger.error(e);
            System.exit(1);
            return "";
        }
    }
}
