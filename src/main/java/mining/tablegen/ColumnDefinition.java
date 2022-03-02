package mining.tablegen;

import java.util.function.Function;

public record ColumnDefinition(
        String header,
        Alignment alignment,
        Function<ContentRow, Object> getCell
) {

}
