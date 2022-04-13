package org.variantsync.diffdetective.tablegen.rows;

import org.variantsync.diffdetective.tablegen.ColumnDefinition;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableGenerator;

import java.util.List;

public class HLine implements Row {
    @Override
    public String toLaTeXRow(List<ColumnDefinition> columns) {
        return TableGenerator.HLINE_ROW;
    }
}
