package mining.tablegen.styles;

import metadata.AtomicPatternCount;
import mining.tablegen.ColumnDefinition;
import mining.tablegen.Row;
import mining.tablegen.TableDefinition;
import mining.tablegen.rows.ContentRow;
import mining.tablegen.rows.DummyRow;
import mining.tablegen.rows.HLine;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;

import java.util.*;
import java.util.function.BiFunction;

import static mining.tablegen.Alignment.*;

public class ShortTable extends TableDefinition {

    private ShortTable() {
        super(new ArrayList<>());
    }

    public static ShortTable Absolute() {
        final ShortTable t = new ShortTable();
        t.columnDefinitions.addAll(columns(t, ShortTable::absoluteCountOf));
        return t;
    }

    public static ShortTable Relative() {
        final ShortTable t = new ShortTable();
        t.columnDefinitions.addAll(columns(t, ShortTable::relativeCountOf));
        return t;
    }

    private static List<ColumnDefinition> columns(final ShortTable t, final BiFunction<AtomicPattern, ContentRow, Number> getPatternCount) {
        final List<ColumnDefinition> cols = new ArrayList<>(List.of(
                col("Name", LEFT, row -> row.dataset().name()),
                col("Domain", LEFT, row -> row.dataset().domain()),
                col("\\#total commits", RIGHT_DASH, row -> t.makeReadable(row.results().totalCommits)),
                col("\\#processed commits", RIGHT, row -> t.makeReadable(row.results().exportedCommits)),
                col("\\#diffs", RIGHT, row -> t.makeReadable(row.results().exportedTrees))
        ));

        for (final AtomicPattern a : ProposedAtomicPatterns.Instance.all()) {
            if (a != ProposedAtomicPatterns.Untouched) {
                cols.add(col(a.getName(), RIGHT, row -> t.makeReadable(getPatternCount.apply(a, row))));
            }
        }

        cols.add(col("runtime (s)", RIGHT, row -> t.makeReadable(row.results().runtimeInSeconds)));

        return cols;
    }

    private static int absoluteCountOf(final AtomicPattern pattern, final ContentRow row) {
        return row.results().atomicPatternCounts.getOccurences().get(pattern).getTotalAmount();
    }

    private static double relativeCountOf(final AtomicPattern pattern, final ContentRow row) {
        final LinkedHashMap<AtomicPattern, AtomicPatternCount.Occurrences> patternOccurrences =
                row.results().atomicPatternCounts.getOccurences();

        int numTotalMatches = 0;
        for (final Map.Entry<AtomicPattern, AtomicPatternCount.Occurrences> occurrence : patternOccurrences.entrySet()) {
            numTotalMatches += occurrence.getValue().getTotalAmount();
        }

        return 100.0 * ((double) patternOccurrences.get(pattern).getTotalAmount()) / ((double) numTotalMatches);
    }

    @Override
    public List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult) {
        final Comparator<ContentRow> larger = (a, b) -> -Integer.compare(a.results().totalCommits, b.results().totalCommits);
        final List<Row> res = rows.stream()
                .sorted(larger)
                .limit(4)
                .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

        res.add(new HLine());

        rows.stream()
                .filter(m -> m.dataset().name().equalsIgnoreCase("Marlin")
                        || m.dataset().name().equalsIgnoreCase("libssh")
                        || m.dataset().name().equalsIgnoreCase("Busybox")
                        || m.dataset().name().equalsIgnoreCase("Godot"))
                .sorted(larger)
                .forEach(res::add);

        res.add(new HLine());
        res.add(new DummyRow());
        res.add(new HLine());
        res.add(new HLine());
        res.add(ultimateResult);

        return res;
    }
}
