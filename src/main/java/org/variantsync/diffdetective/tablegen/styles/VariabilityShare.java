package org.variantsync.diffdetective.tablegen.styles;

import org.variantsync.diffdetective.metadata.ElementaryPatternCount;
import org.variantsync.diffdetective.pattern.ElementaryPattern;
import org.variantsync.diffdetective.pattern.proposed.ProposedElementaryPatterns;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableDefinition;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;

import static org.variantsync.diffdetective.tablegen.Alignment.*;

/**
 * A template of a LaTex table containing the share of variability changing edits for each dataset.
 *
 * <p>Each row represents one dataset and contains the following data:
 * <ul>
 *   <li>the name of the dataset
 *   <li>the total number of edits to variability
 *   <li>a relative occurrence count for each pattern changing variability
 * </ul>
 */
public class VariabilityShare extends TableDefinition {
    private final Supplier<TableDefinition> inner;

    /** Constructs a table definition using {@code inner} to sort and filter the rows. */
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

    /** Returns if the pattern {@code p} should be present in this table. */
    private static boolean isEditToVariability(final ElementaryPattern p) {
        return p != ProposedElementaryPatterns.Untouched && p != ProposedElementaryPatterns.AddToPC && p != ProposedElementaryPatterns.RemFromPC;
    }

    /** Returns the number of occurrences of patterns present in the table. */
    private static Stream<Map.Entry<ElementaryPattern, ElementaryPatternCount.Occurrences>> getVariationalPatterns(final ContentRow row) {
        return row.results().elementaryPatternCounts.getOccurences().entrySet().stream()
                .filter(entry -> isEditToVariability(entry.getKey()));
    }

    /** Compute the total sum of all occurrences of patterns present in this table. */
    private static int countEditsToVariability(final ContentRow row) {
        return getVariationalPatterns(row)
                .map(entry -> entry.getValue().getTotalAmount())
                .reduce(0, Integer::sum);
    }

    /**
     * Compute the number of occurrences of {@code pattern} relative to the patterns actually
     * present in this table.
     */
    private String getRelativeShareOf(final ElementaryPattern pattern, final ContentRow row) {
        final int totalAmount = countEditsToVariability(row);
        return makeReadable(100.0 *  ((double)row.results().elementaryPatternCounts.getOccurences().get(pattern).getTotalAmount()) / ((double) totalAmount)) + "\\%";
    }

    /**
     * Delegates to {@code inner} given in the
     * {@link VariabilityShare(Supplier<TableDefinition>) constructor}.
     * */
    @Override
    public List<? extends Row> sortAndFilter(List<ContentRow> rows, ContentRow ultimateResult) {
        return inner.get().sortAndFilter(rows, ultimateResult);
    }
}
