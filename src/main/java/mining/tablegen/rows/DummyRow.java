package mining.tablegen.rows;

import mining.tablegen.ColumnDefinition;
import mining.tablegen.Row;
import mining.tablegen.TableGenerator;
import util.LaTeX;

import java.util.List;

public class DummyRow implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return "\\multicolumn{" + columns.size() + "}{c}{\\vdots}" + LaTeX.TABLE_ENDROW;
    }
}
