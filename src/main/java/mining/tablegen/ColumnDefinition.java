package mining.tablegen;

import mining.tablegen.rows.ContentRow;

import java.util.function.Function;

public record ColumnDefinition(
        String header,
        Alignment alignment,
        Function<ContentRow, Object> getCell
) {

}
