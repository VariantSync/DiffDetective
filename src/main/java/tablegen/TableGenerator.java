package tablegen;

import tablegen.rows.ContentRow;
import util.LaTeX;
import util.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class TableGenerator {
    protected static final String INDENT = "  ";
    protected static final String HLINE = "\\hline";
    public static final String HLINE_ROW = HLINE + StringUtils.LINEBREAK;
    private final TableDefinition tableDef;

    public TableGenerator(final TableDefinition tableDef) {
        this.tableDef = tableDef;
    }

    public String generateTable(final List<ContentRow> datasets, final ContentRow ultimateResult) {
        final StringBuilder table = new StringBuilder();
        table.append("\\begin{tabular}{");
        final StringBuilder tableHead = new StringBuilder();

        for (int i = 0; i < tableDef.columnDefinitions().size(); ++i) {
            final ColumnDefinition col = tableDef.columnDefinitions().get(i);
            final boolean isLast = i == tableDef.columnDefinitions().size() - 1;

            table.append(col.alignment()).append(" ");
            if (isLast) {
                addLastHeader(tableHead, col.header());
            } else {
                addHeader(tableHead, col.header());
            }
        }
        table.append("}").append(StringUtils.LINEBREAK);
        table.append(tableHead);
        table.append(INDENT).append(HLINE_ROW);

        final List<? extends Row> sorted = tableDef.sortAndFilter(datasets, ultimateResult);
        for (final Row row : sorted) {
            table.append(INDENT).append(row.toLaTeXRow(tableDef.columnDefinitions()));
        }

        table.append("\\end{tabular}").append(StringUtils.LINEBREAK);
        return table.toString();
    }

    private static void addHeader(final StringBuilder builder, final Object val, final String delim) {
        final String valStr = val.toString();
        if (valStr.length() > 6) {
            builder.append("\\resultTableHeader{").append(val).append("}").append(delim);
        } else {
            builder.append(val).append(delim);
        }
    }

    public static void addHeader(final StringBuilder builder, final Object val) {
        addHeader(builder, val, LaTeX.TABLE_SEPARATOR);
    }

    public static void addLastHeader(final StringBuilder builder, final Object val) {
        addHeader(builder, val, LaTeX.TABLE_ENDROW);
    }

    public static void addCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_SEPARATOR);
    }

    public static void addLastCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_ENDROW);
    }

    public static List<ContentRow> alphabeticallySorted(final List<ContentRow> row) {
        final List<ContentRow> copy = new ArrayList<>(row);
        copy.sort((r1, r2) -> r1.dataset().name().compareToIgnoreCase(r2.dataset().name()));
        return copy;
    }
}
