package org.variantsync.diffdetective.variation.diff.serialize.treeformat;

import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphConstants;
import org.variantsync.diffdetective.variation.diff.serialize.LinegraphFormat;

/**
 * Reads and writes {@link Source} from and to line graph.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public interface VariationDiffLabelFormat extends LinegraphFormat {
    /**
     * Converts a label of line graph into a {@link Source}.
     *
     * @param label A string containing the label of the {@link Source}
     * @return The {@link Source} described by this label.
     */
    Source fromLabel(final String label);

    /**
     * Converts a {@link Source} label of line graph.
     *
     * @param variationDiffSource The {@link Source} to be converted
     * @return The corresponding line graph line
     */
    String toLabel(final Source variationDiffSource);

    /**
     * Converts a line describing a graph (starting with "t # ") in line graph format into a {@link Source}.
     *
     * @param lineGraphLine A line from a line graph file starting with "t #"
     * @return The {@link Source} descibed by the label of this line.
     */
    default Source fromLineGraphLine(final String lineGraphLine) {
        return fromLabel(lineGraphLine.substring((LineGraphConstants.LG_TREE_HEADER + " ").length()));
    }

    /**
     * Prepends the {@link LineGraphConstants#LG_TREE_HEADER tree declaration} to a label and return an entire line graph line.
     *
     * @param variationDiffSource The {@link Source} to be converted
     * @return The entire line graph line of a {@link Source}.
     */
    default String toLineGraphLine(final Source variationDiffSource) {
        return LineGraphConstants.LG_TREE_HEADER + " " + toLabel(variationDiffSource);
    }
}
