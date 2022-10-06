package org.variantsync.diffdetective.tablegen.styles;

import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableDefinition;
import org.variantsync.diffdetective.tablegen.TableGenerator;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;
import org.variantsync.diffdetective.tablegen.rows.HLine;

import java.util.ArrayList;
import java.util.List;

import static org.variantsync.diffdetective.tablegen.Alignment.*;

/**
 * A template of a LaTex table containing basic information for each dataset.
 *
 * <p>Each row represents one dataset and contains the following data:
 * <ul>
 *   <li>a dataset description
 *   <li>commit counts
 *   <li>diff counts
 *   <li>edit class counts
 *   <li>processing time
 * </ul>
 */
public class Table1 extends TableDefinition {
    public Table1() {
        super(new ArrayList<>());

        this.columnDefinitions.addAll(List.of(
                col("Name", LEFT, row -> row.dataset().name()),
                col("Domain", LEFT, row -> row.dataset().domain()),
                col("\\#total commits", RIGHT_DASH, row -> makeReadable(row.results().totalCommits)),
                col("\\#processed commits", RIGHT, row -> makeReadable(row.results().exportedCommits)),
                col("\\#diffs", RIGHT, row -> makeReadable(row.results().exportedTrees))
        ));

        for (final EditClass a : ProposedEditClasses.Instance.all()) {
            this.columnDefinitions.add(col(a.getName(), RIGHT, row ->  makeReadable(row.results().editClassCounts.getOccurences().get(a).getTotalAmount())));
        }

        this.columnDefinitions.add(col("runtime (s)", RIGHT, row -> makeReadable(row.results().runtimeInSeconds)));
    }

    /** Sorts {@code rows} alphabetically and appends {@code ultimateResult} to the result. */
    @Override
    public List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult) {
        final List<Row> res = new ArrayList<>(TableGenerator.alphabeticallySorted(rows));

        res.add(new HLine());
        res.add(new HLine());
        res.add(ultimateResult);

        return res;
    }
}
