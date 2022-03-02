package mining.tablegen;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.List;
import java.util.Locale;

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

    public abstract List<Row> sortAndFilter(final List<Row> rows);

    public List<ColumnDefinition> columnDefinitions() {
        return columnDefinitions;
    }
}
