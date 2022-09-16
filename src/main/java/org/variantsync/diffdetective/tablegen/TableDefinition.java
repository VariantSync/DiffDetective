package org.variantsync.diffdetective.tablegen;

import org.variantsync.diffdetective.tablegen.rows.ContentRow;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

/**
 * Definitions for the style of a table.
 * The style of a table includes the format of numbers and the definition of all columns including
 * their name in the header as well as a way to obtain their content..
 */
public abstract class TableDefinition {
    /** Format used to pretty print integers. */
    protected NumberFormat intFormat = NumberFormat.getInstance(Locale.US);
    /** Format used to pretty print doubles. */
    protected NumberFormat doubleFormat = NumberFormat.getInstance(Locale.US);
    /** Definition of all columns in the table. */
    protected final List<ColumnDefinition> columnDefinitions;

    /**
     * Creates a table definition with {@code columnDefinitions} as columns.
     *
     * Numbers are use the US locale and {@code double}s round to one digit after the decimal point
     * a using {@link RoundingMode#HALF_UP}.
     */
    protected TableDefinition(final List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;

        final int fractionDigits = 1;
        doubleFormat.setMaximumFractionDigits(fractionDigits);
        doubleFormat.setMinimumFractionDigits(fractionDigits);
        doubleFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    /**
     * Pretty prints a number as either a {@code int} or {@code double}.
     * The selected type depends on presence of a fractional part in {@code number}.
     *
     * @see makeReadable(int)
     * @see makeReadable(double)
     */
    public String makeReadable(Number number) {
        if (number.doubleValue() == (double) number.intValue()) {
            return intFormat.format(number);
        } else {
            return doubleFormat.format(number);
        }
    }

    /**
     * Pretty prints {@code number} as {@code int}.
     *
     * @see intFormat
     */
    public String makeReadable(int number) {
        return intFormat.format(number);
    }

    /**
     * Pretty prints {@code number} as {@code double}, treating infinity and NaN as absent.
     *
     * @see doubleFormat
     */
    public String makeReadable(double number) {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return "--";
        }
        return doubleFormat.format(number);
    }

    /**
     * Pretty prints {@code number} as {@code long}, treating -1 as absent.
     *
     * @see intFormat
     */
    public String makeReadable(long number) {
        if (number == -1) {
            return "--";
        }
        return intFormat.format(number);
    }

    /**
     * Pretty prints {@code number} as {@code double}, treating infinity and NaN as absent.
     *
     * @see doubleFormat
     */
    public String makeReadable(String number) {
        if (number.isBlank()) {
            return number;
        }

        try {
            return makeReadable(doubleFormat.parse(number));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns a list of all rows that should appear in the table.
     * The list of rows is final after this step, only the header is prepended.
     *
     * @param rows the data to be sorted and filtered
     * @param ultimateResult a row containing accumulated results of {@code rows}
     */
    public abstract List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult);

    /** Returns the list of columns in the table. */
    public List<ColumnDefinition> columnDefinitions() {
        return columnDefinitions;
    }

    /** Constructs a new column definition. */
    public static ColumnDefinition col(
            String header,
            Alignment alignment,
            Function<ContentRow, Object> getCell)
    {
        return new ColumnDefinition(header, alignment, getCell);
    }
}
