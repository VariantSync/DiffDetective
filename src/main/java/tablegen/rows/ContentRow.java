package tablegen.rows;

import analysis.AnalysisResult;
import analysis.AutomationResult;
import datasets.DatasetDescription;
import tablegen.ColumnDefinition;
import tablegen.Row;
import tablegen.TableGenerator;

import java.util.List;

public record ContentRow(
        DatasetDescription dataset,
        AnalysisResult results,
        AutomationResult automationResult
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
