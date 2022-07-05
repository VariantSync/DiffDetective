package org.variantsync.diffdetective.util;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Interface for values that can be converted to a CSV row.
 * A row in comma separated value (CSV) tables consists of values, potentially quoted with double
 * quotes, and a delimiter between each value. Multiple rows are delimited by new lines and can be
 * generated using {@link toCSV(Stream<? extends CSV> collection)}.
 */
public interface CSV {
    /** The default delimiter for DiffDetective tables. */
    String DEFAULT_CSV_DELIMITER = ";";

    /** Convert this value into a CSV row using the {@link DEFAULT_CSV_DELIMITER}. */
    default String toCSV() {
        return toCSV(DEFAULT_CSV_DELIMITER);
    }

    /** Convert this value into a CSV row using a custom CSV {@code delimiter}. */
    String toCSV(final String delimiter);

    /**
     * Convert a {@code collection} of CSV values into a CSV table.
     * Each value will become a single row in the resulting table. As new line
     * {@link StringUtils#LINEBREAK} is used.
     */
    static String toCSV(final Collection<? extends CSV> collection) {
        return toCSV(collection.stream());
    }

    /**
     * Convert a {@code collection} of CSV values into a CSV table.
     * Each value will become a single row in the resulting table. As new line
     * {@link StringUtils#LINEBREAK} is used.
     */
    static String toCSV(final Stream<? extends CSV> collection) {
        return collection
                .map(CSV::toCSV)
                .collect(Collectors.joining(StringUtils.LINEBREAK));
    }
}
