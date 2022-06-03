package org.variantsync.diffdetective.util;

import java.util.Collection;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface CSV {
    String DEFAULT_CSV_DELIMITER = ";";

    default String toCSV() {
        return toCSV(DEFAULT_CSV_DELIMITER);
    }

    String toCSV(final String delimiter);

    static String toCSV(final Collection<? extends CSV> collection) {
        return toCSV(collection.stream());
    }

    static String toCSV(final Stream<? extends CSV> collection) {
        return collection
                .map(CSV::toCSV)
                .collect(Collectors.joining(StringUtils.LINEBREAK));
    }
}
