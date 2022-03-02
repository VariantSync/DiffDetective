package mining.tablegen;

import java.util.List;

public interface Row {
    String toLaTeXRow(final List<ColumnDefinition> columns);
}
