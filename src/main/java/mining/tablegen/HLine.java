package mining.tablegen;

import java.util.List;

public class HLine implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return TableGenerator.HLINE_ROW;
    }
}
