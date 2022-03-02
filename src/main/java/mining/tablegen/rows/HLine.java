package mining.tablegen.rows;

import mining.tablegen.ColumnDefinition;
import mining.tablegen.Row;
import mining.tablegen.TableGenerator;

import java.util.List;

public class HLine implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return TableGenerator.HLINE_ROW;
    }
}
