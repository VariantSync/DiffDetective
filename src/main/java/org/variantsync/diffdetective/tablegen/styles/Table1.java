package org.variantsync.diffdetective.tablegen.styles;

import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableDefinition;
import org.variantsync.diffdetective.tablegen.TableGenerator;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;
import org.variantsync.diffdetective.tablegen.rows.HLine;

import java.util.ArrayList;
import java.util.List;

import static org.variantsync.diffdetective.tablegen.Alignment.*;

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

        for (final ElementaryPattern a : ProposedElementaryPatterns.Instance.all()) {
            this.columnDefinitions.add(col(a.getName(), RIGHT, row ->  makeReadable(row.results().elementaryPatternCounts.getOccurences().get(a).getTotalAmount())));
        }

        this.columnDefinitions.add(col("runtime (s)", RIGHT, row -> makeReadable(row.results().runtimeInSeconds)));
    }

    @Override
    public List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult) {
        final List<Row> res = new ArrayList<>(TableGenerator.alphabeticallySorted(rows));

        res.add(new HLine());
        res.add(new HLine());
        res.add(ultimateResult);

        return res;
    }
}
