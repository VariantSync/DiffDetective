package org.variantsync.diffdetective.tablegen.rows;

import org.variantsync.diffdetective.analysis.AnalysisResult;
import org.variantsync.diffdetective.analysis.AutomationResult;
import org.variantsync.diffdetective.analysis.AnalysisResult.ResultKey;
import org.variantsync.diffdetective.datasets.DatasetDescription;
import org.variantsync.diffdetective.metadata.Metadata;
import org.variantsync.diffdetective.tablegen.ColumnDefinition;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableGenerator;

import java.util.List;

/**
 * A row in a LaTex table containing the analysis data of a single dataset.
 *
 * @param dataset the dataset this row belongs to
 * @param results the results of the analysis
 * @param automationResult metadata about the analysis
 */
public record ContentRow(
        DatasetDescription dataset,
        AnalysisResult results,
        AutomationResult automationResult
) implements Row {
    public <T extends Metadata<T>> T get(ResultKey<T> resultKey) {
        return results.get(resultKey);
    }

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
