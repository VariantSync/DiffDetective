package tablegen;

import java.util.List;

@FunctionalInterface
public interface Row {
    String toLaTeXRow(final List<ColumnDefinition> columns);
}
