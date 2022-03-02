package mining.tablegen;

import mining.tablegen.rows.ContentRow;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

public abstract class TableDefinition {
    protected NumberFormat numberFormat = NumberFormat.getInstance(Locale.US);
    protected final List<ColumnDefinition> columnDefinitions;

    protected TableDefinition(final List<ColumnDefinition> columnDefinitions) {
        this.columnDefinitions = columnDefinitions;
    }

    public String makeReadable(int number) {
        return numberFormat.format(number);
    }

    public String makeReadable(double number) {
        return numberFormat.format(number);
    }

    public String makeReadable(String number) {
        if (number.isBlank()) {
            return number;
        }

        try {
            return makeReadable(numberFormat.parse(number).doubleValue());
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
