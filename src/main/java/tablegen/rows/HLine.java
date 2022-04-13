package tablegen.rows;

import tablegen.ColumnDefinition;
import tablegen.Row;
import tablegen.TableGenerator;

import java.util.List;

public class HLine implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return TableGenerator.HLINE_ROW;
    }
}
