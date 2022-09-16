package org.variantsync.diffdetective.tablegen;

import org.variantsync.diffdetective.tablegen.rows.ContentRow;
import org.variantsync.diffdetective.util.LaTeX;
import org.variantsync.diffdetective.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

/** Converter of analysis results to LaTex tables. */
public class TableGenerator {
    /** The indentation used for the rows of the generated table. */
    protected static final String INDENT = "  ";
    /** LaTex code for a horizontal line to separate two rows. */
    protected static final String HLINE = "\\hline";
    /** LaTex code for a horizontal line to separate two rows as a pseudo row. */
    public static final String HLINE_ROW = HLINE + StringUtils.LINEBREAK;
    /** Style and column definitions of the table. */
    private final TableDefinition tableDef;

    /** Constructs a {@code TableGenerator} constructing a table according to {@code tableDef}. */
    public TableGenerator(final TableDefinition tableDef) {
        this.tableDef = tableDef;
    }

    /**
     * Convert {@code datasets} to a LaTex table according to the template represented by this
     * class.
     * The column headers and the data they contain are configurable by the table definition given
     * in the {@link TableGenerator constructor}.
     *
     * @param datasets the dataset to be converted
     * @param ultimateResult a summary row added according to the table definition
     * @return the LaTex code for a table representing {@code datasets}
     */
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

    /**
     * Add a single header cell, annotating it appropriately.
     * The macro {@code resultTableHeader} has to be defined to format the lines containing header
     * strings longer than 6 characters.
     *
     * @param builder the header row to which the cell is added
     * @param val the content of the header cell
     * @param delim the delimiter between this cell and the next cell
     */
    private static void addHeader(final StringBuilder builder, final Object val, final String delim) {
        final String valStr = val.toString();
        if (valStr.length() > 6) {
            builder.append("\\resultTableHeader{").append(val).append("}").append(delim);
        } else {
            builder.append(val).append(delim);
        }
    }

    /**
     * Adds a single header cell to the row {@code builder} with {@code val} as data.
     * This cell can't be the last header in the row. In that case it has to added by
     * {@link addLastHeader}.
     */
    public static void addHeader(final StringBuilder builder, final Object val) {
        addHeader(builder, val, LaTeX.TABLE_SEPARATOR);
    }

    /** Adds the last header cell of the row {@code builder} with {@code val} as data. */
    public static void addLastHeader(final StringBuilder builder, final Object val) {
        addHeader(builder, val, LaTeX.TABLE_ENDROW);
    }

    /**
     * Adds a single cell to the row {@code builder} with {@code val} as data.
     * This cell can't be the last cell in the row. In that case it has to added by
     * {@link addLastCell}.
     *
     * @see ContentRow
     */
    public static void addCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_SEPARATOR);
    }

    /**
     * Adds the last cell of the row {@code builder} with {@code val} as data.
     *
     * @see ContentRow
     */
    public static void addLastCell(final StringBuilder builder, final Object val) {
        builder.append(val).append(LaTeX.TABLE_ENDROW);
    }

    /** Returns a new alphabetically sorted list. */
    public static List<ContentRow> alphabeticallySorted(final List<ContentRow> row) {
        final List<ContentRow> copy = new ArrayList<>(row);
        copy.sort((r1, r2) -> r1.dataset().name().compareToIgnoreCase(r2.dataset().name()));
        return copy;
    }
}
