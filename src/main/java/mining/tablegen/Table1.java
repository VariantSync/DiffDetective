package mining.tablegen;

import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;

import java.util.ArrayList;
import java.util.List;

import static mining.tablegen.Alignment.*;

public class Table1 extends TableDefinition {
    public Table1() {
        super(new ArrayList<>());

        this.columnDefinitions.addAll(List.of(
                new ColumnDefinition("Name", LEFT, row -> row.dataset().name()),
                new ColumnDefinition("Domain", LEFT, row -> row.dataset().domain()),
                new ColumnDefinition("\\#total commits", RIGHT_DASH, row -> makeReadable(row.results().totalCommits)),
                new ColumnDefinition("\\#processed commits", RIGHT, row -> makeReadable(row.results().exportedCommits)),
                new ColumnDefinition("\\#mined tree diffs", RIGHT, row -> makeReadable(row.results().exportedTrees))
        ));

        for (final AtomicPattern a : ProposedAtomicPatterns.Instance.all()) {
            this.columnDefinitions.add(new ColumnDefinition(a.getName(), RIGHT, row ->  makeReadable(row.results().atomicPatternCounts.getOccurences().get(a).getTotalAmount())));
        }

        this.columnDefinitions.add(new ColumnDefinition("runtime (s)", RIGHT, row -> row.results().runtimeInSeconds));
    }

    @Override
    public List<Row> sortAndFilter(List<Row> rows) {
        return TableGenerator.alphabeticallySorted(rows);
    }
}
