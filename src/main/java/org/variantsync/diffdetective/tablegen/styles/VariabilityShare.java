package org.variantsync.diffdetective.tablegen.styles;

import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
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
 *   <li>a relative occurrence count for each edit class changing variability
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

        for (final EditClass a : ProposedEditClasses.Instance.all()) {
            if (isEditToVariability(a)) {
                this.columnDefinitions.add(col(a.getName(), RIGHT, row -> getRelativeShareOf(a, row)));
            }
        }
    }

    /** Returns if the edit class {@code c} should be present in this table. */
    private static boolean isEditToVariability(final EditClass c) {
        return c != ProposedEditClasses.Untouched && c != ProposedEditClasses.AddToPC && c != ProposedEditClasses.RemFromPC;
    }

    /** Returns the number of occurrences of edit classes present in the table. */
    private static Stream<Map.Entry<EditClass, EditClassCount.Occurrences>> getVariationalEditClasses(final ContentRow row) {
        return row.results().editClassCounts.getOccurences().entrySet().stream()
                .filter(entry -> isEditToVariability(entry.getKey()));
    }

    /** Compute the total sum of all occurrences of edit classes present in this table. */
    private static int countEditsToVariability(final ContentRow row) {
        return getVariationalEditClasses(row)
                .map(entry -> entry.getValue().getTotalAmount())
                .reduce(0, Integer::sum);
    }

    /**
     * Compute the number of occurrences of {@code editClass} relative to the edit classes actually
     * present in this table.
     */
    private String getRelativeShareOf(final EditClass editClass, final ContentRow row) {
        final int totalAmount = countEditsToVariability(row);
        return makeReadable(100.0 *  ((double)row.results().editClassCounts.getOccurences().get(editClass).getTotalAmount()) / ((double) totalAmount)) + "\\%";
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
