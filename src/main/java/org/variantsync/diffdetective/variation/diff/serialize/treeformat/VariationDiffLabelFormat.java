package org.variantsync.diffdetective.variation.diff.serialize.treeformat;

import org.variantsync.diffdetective.variation.diff.serialize.LineGraphConstants;
import org.variantsync.diffdetective.variation.diff.serialize.LinegraphFormat;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

/**
 * Reads and writes {@link VariationDiffSource} from and to line graph.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public interface VariationDiffLabelFormat extends LinegraphFormat {
	/**
	 * Converts a label of line graph into a {@link VariationDiffSource}.
	 * 
	 * @param label A string containing the label of the {@link VariationDiffSource}
	 * @return The {@link VariationDiffSource} descibed by this label.
	 */
	VariationDiffSource fromLabel(final String label);
	
	/**
	 * Converts a {@link VariationDiffSource} label of line graph.
	 * 
	 * @param variationDiffSource The {@link VariationDiffSource} to be converted
	 * @return The corresponding line graph line
	 */
	String toLabel(final VariationDiffSource variationDiffSource);
	
	/**
     * Converts a line describing a graph (starting with "t # ") in line graph format into a {@link VariationDiffSource}.
	 *
     * @param lineGraphLine A line from a line graph file starting with "t #"
	 * @return The {@link VariationDiffSource} descibed by the label of this line.
	 */
	default VariationDiffSource fromLineGraphLine(final String lineGraphLine) {
		return fromLabel(lineGraphLine.substring((LineGraphConstants.LG_TREE_HEADER + " ").length()));
	}
	
	/**
	 * Prepends the {@link LineGraphConstants#LG_TREE_HEADER tree declaration} to a label and return an entire line graph line.
	 * 
	 * @param variationDiffSource The {@link VariationDiffSource} to be converted
	 * @return The entire line graph line of a {@link VariationDiffSource}.
	 */
    default String toLineGraphLine(final VariationDiffSource variationDiffSource) {
		return LineGraphConstants.LG_TREE_HEADER + " " + toLabel(variationDiffSource);
	}
}
