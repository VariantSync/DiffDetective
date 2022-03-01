package mining.tablegen;

import java.util.function.Function;

public record ColumnDefinition(
        String header,
        Alignment alignment,
        Function<Row, Object> getCell
) {

}
