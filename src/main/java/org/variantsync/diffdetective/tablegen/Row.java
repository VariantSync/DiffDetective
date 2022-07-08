package org.variantsync.diffdetective.tablegen;

import java.util.List;

/** A row like entity that can be represented by a single row in code LaTex. */
@FunctionalInterface
public interface Row {
    /**
     * Convert this row to LaTex code.
     *
     * @param columns information about the columns in this row
     */
    String toLaTeXRow(final List<ColumnDefinition> columns);
}
