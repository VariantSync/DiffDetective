package tablegen.styles;

import metadata.AtomicPatternCount;
import org.apache.commons.lang3.function.TriFunction;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;
import tablegen.ColumnDefinition;
import tablegen.Row;
import tablegen.TableDefinition;
import tablegen.rows.ContentRow;
import tablegen.rows.HLine;
import util.LaTeX;

import java.util.*;

import static tablegen.Alignment.*;

public class ShortTable extends TableDefinition {
    private final boolean filtered;

    private ShortTable(boolean filtered) {
        super(new ArrayList<>());
        this.filtered = filtered;
    }

    public static ShortTable Absolute(boolean filtered) {
        final ShortTable t = new ShortTable(filtered);
        t.columnDefinitions.addAll(columns(t, ShortTable::absoluteCountOf));
        return t;
    }

    public static ShortTable Relative(boolean filtered) {
        final ShortTable t = new ShortTable(filtered);
        t.columnDefinitions.addAll(columns(t, ShortTable::relativeCountOf));
        return t;
    }

    private static List<ColumnDefinition> columns(final ShortTable t, final TriFunction<ShortTable, AtomicPattern, ContentRow, String> getPatternCount) {
        final List<ColumnDefinition> cols = new ArrayList<>(List.of(
                col("Name", LEFT, row -> row.dataset().name().toLowerCase(Locale.US)),
                col("Domain", LEFT_DASH, row -> row.dataset().domain()),
                col("\\#total\\\\ commits", RIGHT, row -> t.makeReadable(row.results().totalCommits)),
                col("\\#processed commits", RIGHT, row -> t.makeReadable(row.results().exportedCommits)),
                col("\\#diffs", RIGHT, row -> t.makeReadable(row.results().exportedTrees)),
                col("\\#artifact nodes", RIGHT_DASH, row -> t.makeReadable(row
                        .results()
                        .atomicPatternCounts
                        .getOccurences()
                        .values().stream()
                        .map(AtomicPatternCount.Occurrences::getTotalAmount)
                        .reduce(0, Integer::sum)
                ))
        ));

        for (final AtomicPattern a : ProposedAtomicPatterns.Instance.all()) {
            if (a != ProposedAtomicPatterns.Untouched) {
                cols.add(col(a.getName(), RIGHT, row -> getPatternCount.apply(t, a, row)));
            }
        }

        cols.add(col("runtime", DASH_RIGHT, row -> t.makeReadable(row.results().runtimeInSeconds) + "s"));
        cols.add(col("avg. runtime per\\\\ processed commit", RIGHT, row -> t.makeReadable(row.automationResult().avgTimeMS()) + "ms"));
        cols.add(col("median runtime per\\\\ processed commit", RIGHT, row -> t.makeReadable(row.automationResult().median().milliseconds()) + "ms"));

        return cols;
    }

    private static String absoluteCountOf(final ShortTable t, final AtomicPattern pattern, final ContentRow row) {
        return t.makeReadable(row.results().atomicPatternCounts.getOccurences().get(pattern).getTotalAmount());
    }

    private static String relativeCountOf(final ShortTable t, final AtomicPattern pattern, final ContentRow row) {
        final LinkedHashMap<AtomicPattern, AtomicPatternCount.Occurrences> patternOccurrences =
                row.results().atomicPatternCounts.getOccurences();

        int numTotalMatches = 0;
        for (final Map.Entry<AtomicPattern, AtomicPatternCount.Occurrences> occurrence : patternOccurrences.entrySet()) {
            numTotalMatches += occurrence.getValue().getTotalAmount();
        }

        return t.makeReadable(100.0 * ((double) patternOccurrences.get(pattern).getTotalAmount()) / ((double) numTotalMatches)) + "\\%";
    }

    @Override
    public List<? extends Row> sortAndFilter(final List<ContentRow> rows, final ContentRow ultimateResult) {
        final List<Row> res;

        if (filtered) {
            final Comparator<ContentRow> larger = (a, b) -> -Integer.compare(a.results().totalCommits, b.results().totalCommits);
            res = rows.stream()
                    .sorted(larger)
                    .limit(4)
                    .collect(ArrayList::new, ArrayList::add, ArrayList::addAll);

//            res.add(new HLine());
//            res.add(cols -> "\\multicolumn{2}{c}{36 other systems} & \\multicolumn{" + (cols.size() - 2) + "}{c}{\\vdots}" + LaTeX.TABLE_ENDROW);
            final String innerColumnHeader = ">{\\centering}m{.333333\\linewidth}";
            final String vdots = "$\\vdots$";
            res.add(cols -> "\\multicolumn{" + cols.size() + "}{c}{\\begin{tabular}{"
                    + innerColumnHeader + innerColumnHeader + innerColumnHeader + "} "
                    + vdots + " & 36 other systems & " + vdots
                    + "\\end{tabular}}" + LaTeX.TABLE_ENDROW);

            res.add(new HLine());

            rows.stream()
                    .filter(m -> m.dataset().name().equalsIgnoreCase("Marlin")
                            || m.dataset().name().equalsIgnoreCase("libssh")
                            || m.dataset().name().equalsIgnoreCase("Busybox")
                            || m.dataset().name().equalsIgnoreCase("Godot"))
                    .sorted(larger)
                    .forEach(res::add);
        } else {
            res = new ArrayList<>(rows);
        }

        res.add(new HLine());
        res.add(new HLine());
        res.add(ultimateResult);

        return res;
    }
}
