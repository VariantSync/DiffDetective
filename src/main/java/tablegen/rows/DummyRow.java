package tablegen.rows;

import tablegen.ColumnDefinition;
import tablegen.Row;
import util.LaTeX;

import java.util.List;

public class DummyRow implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return "\\multicolumn{" + columns.size() + "}{c}{\\vdots}" + LaTeX.TABLE_ENDROW;
    }
}
