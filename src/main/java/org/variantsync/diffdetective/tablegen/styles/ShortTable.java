package org.variantsync.diffdetective.tablegen.styles;

import org.apache.commons.lang3.function.TriFunction;
import org.variantsync.diffdetective.metadata.EditClassCount;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.tablegen.ColumnDefinition;
import org.variantsync.diffdetective.tablegen.Row;
import org.variantsync.diffdetective.tablegen.TableDefinition;
import org.variantsync.diffdetective.tablegen.rows.ContentRow;
import org.variantsync.diffdetective.tablegen.rows.HLine;
import org.variantsync.diffdetective.util.LaTeX;

import java.util.*;

import static org.variantsync.diffdetective.tablegen.Alignment.*;

/**
 * A template of a LaTex table containing a row with much information for each dataset.
 * The table can be shortened by filtering the rows to only keep the biggest datasets.
 *
 * <p>Each row represents one dataset and contains the following data:
 * <ul>
 *   <li>a dataset description
 *   <li>commit counts
 *   <li>diff counts
 *   <li>the number of artifact nodes
 *   <li>edit class counts, either absolute or relative to the total count of all edit classes
 *   <li>processing time
 * </ul>
 *
 */
public class ShortTable extends TableDefinition {
    private final boolean filtered;

    /**
     * Constructs a table template which shortens the table by filtering the rows iff
     * {@code filtered} is {@code true}.
     */
    private ShortTable(boolean filtered) {
        super(new ArrayList<>());
        this.filtered = filtered;
    }

    /**
     * Constructs a table template with absolute edit class counts.
     *
     * @param filtered if {@code true}, the rows will be filtered to only contain the biggest
     * datasets
     */
    public static ShortTable Absolute(boolean filtered) {
        final ShortTable t = new ShortTable(filtered);
        t.columnDefinitions.addAll(columns(t, ShortTable::absoluteCountOf));
        return t;
    }

    /**
     * Constructs a table template with relative edit class counts.
     *
     * @param filtered if {@code true}, the rows will be filtered to only contain the biggest
     * datasets
     */
    public static ShortTable Relative(boolean filtered) {
        final ShortTable t = new ShortTable(filtered);
        t.columnDefinitions.addAll(columns(t, ShortTable::relativeCountOf));
        return t;
    }

    /**
     * Returns a list of all columns contained in this table.
     *
     * @param t instance to this class, used for formatting with {@link makeReadable}
     * @param getEditClassCount function to extract the edit class count of a row
     */
    private static List<ColumnDefinition> columns(final ShortTable t, final TriFunction<ShortTable, EditClass, ContentRow, String> getEditClassCount) {
        final List<ColumnDefinition> cols = new ArrayList<>(List.of(
                col("Name", LEFT, row -> row.dataset().name().toLowerCase(Locale.US)),
                col("Domain", LEFT_DASH, row -> row.dataset().domain()),
                col("\\#total\\\\ commits", RIGHT, row -> t.makeReadable(row.results().totalCommits)),
                col("\\#processed commits", RIGHT, row -> t.makeReadable(row.results().exportedCommits)),
                col("\\#diffs", RIGHT, row -> t.makeReadable(row.results().exportedTrees)),
                col("\\#artifact nodes", RIGHT_DASH, row -> t.makeReadable(row
                        .results()
                        .editClassCounts
                        .getOccurences()
                        .values().stream()
                        .map(EditClassCount.Occurrences::getTotalAmount)
                        .reduce(0, Integer::sum)
                ))
        ));

        for (final EditClass a : ProposedEditClasses.Instance.all()) {
            if (a != ProposedEditClasses.Untouched) {
                cols.add(col(a.getName(), RIGHT, row -> getEditClassCount.apply(t, a, row)));
            }
        }

        cols.add(col("runtime", DASH_RIGHT, row -> t.makeReadable(row.results().runtimeInSeconds) + "s"));
        cols.add(col("avg. runtime~/\\\\ processed commit", RIGHT, row -> t.makeReadable(row.automationResult().avgTimeMS()) + "ms"));
        cols.add(col("median runtime~/\\\\ processed commit", RIGHT, row -> t.makeReadable(row.automationResult().median().milliseconds()) + "ms"));

        return cols;
    }

    /**
     * Returns a formatted string of the absolute number of occurrences of {@code editClass} in
     * {@code row}.
     * The signature of this method is suitable to be passed to {@link column}.
     *
     * @param t an instance of this class contained in the column definition
     * @param editClass the edit class to count
     * @param row the data to count {@code editClass} in
     * @see column
     */
    private static String absoluteCountOf(final ShortTable t, final EditClass editClass, final ContentRow row) {
        return t.makeReadable(row.results().editClassCounts.getOccurences().get(editClass).getTotalAmount());
    }

    /**
     * Returns a formatted string of the relative number of occurrences of {@code editClass} in
     * {@code row}.
     * The signature of this method is suitable to be passed to {@link column}.
     *
     * @param t an instance of this class contained in the column definition
     * @param editClass the edit class to count
     * @param row the data to count {@code editClass} in
     * @see column
     */
    private static String relativeCountOf(final ShortTable t, final EditClass editClass, final ContentRow row) {
        final LinkedHashMap<EditClass, EditClassCount.Occurrences> editClassOccurrences =
                row.results().editClassCounts.getOccurences();

        int numTotalMatches = 0;
        for (final Map.Entry<EditClass, EditClassCount.Occurrences> occurrence : editClassOccurrences.entrySet()) {
            numTotalMatches += occurrence.getValue().getTotalAmount();
        }

        return t.makeReadable(100.0 * ((double) editClassOccurrences.get(editClass).getTotalAmount()) / ((double) numTotalMatches)) + "\\%";
    }

    /**
     * Conditionally sorts and limits the rows by the number of commits in their dataset.
     *
     * @see Absolute(boolean)
     * @see Relative(boolean)
     */
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
