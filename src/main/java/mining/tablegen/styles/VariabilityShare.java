package mining.tablegen.styles;

import metadata.AtomicPatternCount;
import mining.tablegen.ColumnDefinition;
import mining.tablegen.Row;
import mining.tablegen.TableDefinition;
import mining.tablegen.rows.ContentRow;
import pattern.atomic.AtomicPattern;
import pattern.atomic.proposed.ProposedAtomicPatterns;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static mining.tablegen.Alignment.*;

public class VariabilityShare extends TableDefinition {
    private final Supplier<TableDefinition> inner;

    public VariabilityShare(final Supplier<TableDefinition> inner) {
        super(new ArrayList<>());
        this.inner = inner;

        this.columnDefinitions.addAll(List.of(
                col("Name", LEFT_DASH, row -> row.dataset().name()),
                col("\\#edits to\\\\ variability", RIGHT_DASH, row -> makeReadable(countEditsToVariability(row)))
        ));

        for (final AtomicPattern a : ProposedAtomicPatterns.Instance.all()) {
            if (isEditToVariability(a)) {
                this.columnDefinitions.add(col(a.getName(), RIGHT, row -> getRelativeShareOf(a, row)));
            }
        }
    }

    private static boolean isEditToVariability(final AtomicPattern p) {
        return p != ProposedAtomicPatterns.Untouched && p != ProposedAtomicPatterns.AddToPC && p != ProposedAtomicPatterns.RemFromPC;
    }

    private static Stream<Map.Entry<AtomicPattern, AtomicPatternCount.Occurrences>> getVariationalPatterns(final ContentRow row) {
        return row.results().atomicPatternCounts.getOccurences().entrySet().stream()
                .filter(entry -> isEditToVariability(entry.getKey()));
    }

    private static int countEditsToVariability(final ContentRow row) {
        return getVariationalPatterns(row)
                .map(entry -> entry.getValue().getTotalAmount())
                .reduce(0, Integer::sum);
    }

    private String getRelativeShareOf(final AtomicPattern pattern, final ContentRow row) {
        final int totalAmount = countEditsToVariability(row);
        return makeReadable(100.0 *  ((double)row.results().atomicPatternCounts.getOccurences().get(pattern).getTotalAmount()) / ((double) totalAmount)) + "\\%";
    }

    @Override
    public List<? extends Row> sortAndFilter(List<ContentRow> rows, ContentRow ultimateResult) {
        return inner.get().sortAndFilter(rows, ultimateResult);
    }
}
