package org.variantsync.diffdetective.tablegen;

import org.variantsync.diffdetective.tablegen.rows.ContentRow;

import java.util.function.Function;

public record ColumnDefinition(
        String header,
        Alignment alignment,
        Function<ContentRow, Object> getCell
) {

}
