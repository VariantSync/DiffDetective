package mining.tablegen.rows;

import mining.DiffTreeMiningResult;
import mining.dataset.MiningDataset;
import mining.tablegen.ColumnDefinition;
import mining.tablegen.Row;
import mining.tablegen.TableGenerator;

import java.util.List;

public record ContentRow(
        MiningDataset dataset,
        DiffTreeMiningResult results
) implements Row {
    @Override
    public String toLaTeXRow(final List<ColumnDefinition> columns) {
        final StringBuilder lineBuilder = new StringBuilder();

        for (int i = 0; i < columns.size(); ++i) {
            final ColumnDefinition col = columns.get(i);
            final boolean isLast = i == columns.size() - 1;
            if (isLast) {
                TableGenerator.addLastCell(lineBuilder, col.getCell().apply(this));
            } else {
                TableGenerator.addCell(lineBuilder, col.getCell().apply(this));
            }
        }

        return lineBuilder.toString();
    }
}
