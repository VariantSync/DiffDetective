package org.variantsync.diffdetective.tablegen;

import org.variantsync.diffdetective.tablegen.rows.ContentRow;

import java.util.function.Function;

/**
 * Properties of a column in a LaTex table.
 *
 * @param header the name of column
 * @param alignment how the content in that column is aligned horizontally
 * @param getCell extracts the content of a row which belongs into this column
 */
public record ColumnDefinition(
        String header,
        Alignment alignment,
        Function<ContentRow, Object> getCell
) {

}
