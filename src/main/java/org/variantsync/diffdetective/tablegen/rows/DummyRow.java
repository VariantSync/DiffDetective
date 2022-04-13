package org.variantsync.diffdetective.tablegen.rows;

import org.variantsync.diffdetective.tablegen.ColumnDefinition;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.util.LaTeX;

import java.util.List;

public class DummyRow implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return "\\multicolumn{" + columns.size() + "}{c}{\\vdots}" + LaTeX.TABLE_ENDROW;
    }
}
