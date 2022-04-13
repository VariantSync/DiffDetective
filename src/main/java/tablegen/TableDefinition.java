package tablegen;

import tablegen.rows.ContentRow;

import java.math.RoundingMode;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public abstract class TableDefinition {
    protected NumberFormat intFormat = NumberFormat.getInstance(Locale.US);
    protected NumberFormat doubleFormat = NumberFormat.getInstance(Locale.US);
    protected final List<ColumnDefinition> columnDefinitions;

    protected TableDefinition(final List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;

        final int fractionDigits = 1;
        doubleFormat.setMaximumFractionDigits(fractionDigits);
        doubleFormat.setMinimumFractionDigits(fractionDigits);
        doubleFormat.setRoundingMode(RoundingMode.HALF_UP);
    }

    public String makeReadable(Number number) {
        if (number.doubleValue() == (double) number.intValue()) {
            return intFormat.format(number);
        } else {
            return doubleFormat.format(number);
        }
    }

    public String makeReadable(int number) {
        return intFormat.format(number);
    }

    public String makeReadable(double number) {
        if (Double.isInfinite(number) || Double.isNaN(number)) {
            return "--";
        }
        return doubleFormat.format(number);
    }

    public String makeReadable(long number) {
        if (number == -1) {
            return "--";
        }
        return intFormat.format(number);
    }

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

    public abstract List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult);

    public List<ColumnDefinition> columnDefinitions() {
        return columnDefinitions;
    }

    public static ColumnDefinition col(
            String header,
            Alignment alignment,
            Function<ContentRow, Object> getCell)
    {
        return new ColumnDefinition(header, alignment, getCell);
    }
}
