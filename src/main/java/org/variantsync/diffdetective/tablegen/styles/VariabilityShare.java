package org.variantsync.diffdetective.tablegen.styles;

import org.variantsync.diffdetective.metadata.ElementaryPatternCount;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableDefinition;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.variantsync.diffdetective.tablegen.Alignment.*;

public class VariabilityShare extends TableDefinition {
    private final Supplier<TableDefinition> inner;

    public VariabilityShare(final Supplier<TableDefinition> inner) {
        super(new ArrayList<>());
        this.inner = inner;

        this.columnDefinitions.addAll(List.of(
                col("Name", LEFT_DASH, row -> row.dataset().name()),
                col("\\#edits to\\\\ variability", RIGHT_DASH, row -> makeReadable(countEditsToVariability(row)))
        ));

        for (final ElementaryPattern a : ProposedElementaryPatterns.Instance.all()) {
            if (isEditToVariability(a)) {
                this.columnDefinitions.add(col(a.getName(), RIGHT, row -> getRelativeShareOf(a, row)));
            }
        }
    }

    private static boolean isEditToVariability(final ElementaryPattern p) {
        return p != ProposedElementaryPatterns.Untouched && p != ProposedElementaryPatterns.AddToPC && p != ProposedElementaryPatterns.RemFromPC;
    }

    private static Stream<Map.Entry<ElementaryPattern, ElementaryPatternCount.Occurrences>> getVariationalPatterns(final ContentRow row) {
        return row.results().elementaryPatternCounts.getOccurences().entrySet().stream()
                .filter(entry -> isEditToVariability(entry.getKey()));
    }

    private static int countEditsToVariability(final ContentRow row) {
        return getVariationalPatterns(row)
                .map(entry -> entry.getValue().getTotalAmount())
                .reduce(0, Integer::sum);
    }

    private String getRelativeShareOf(final ElementaryPattern pattern, final ContentRow row) {
        final int totalAmount = countEditsToVariability(row);
        return makeReadable(100.0 *  ((double)row.results().elementaryPatternCounts.getOccurences().get(pattern).getTotalAmount()) / ((double) totalAmount)) + "\\%";
    }

    @Override
    public List<? extends Row> sortAndFilter(List<ContentRow> rows, ContentRow ultimateResult) {
        return inner.get().sortAndFilter(rows, ultimateResult);
    }
}
