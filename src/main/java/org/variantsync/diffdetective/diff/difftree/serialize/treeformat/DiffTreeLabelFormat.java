package org.variantsync.diffdetective.diff.difftree.serialize.treeformat;

import org.variantsync.diffdetective.diff.difftree.DiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.LinegraphFormat;

/**
 * Reads and writes {@link DiffTreeSource} from and to line graph.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public interface DiffTreeLabelFormat extends LinegraphFormat {
	/**
	 * Converts a label of line graph into a {@link DiffTreeSource}.
	 * 
	 * @param label A string containing the label of the {@link DiffTreeSource}
	 * @return The {@link DiffTreeSource} descibed by this label.
	 */
	DiffTreeSource fromLabel(final String label);
	
	/**
	 * Converts a {@link DiffTreeSource} label of line graph.
	 * 
	 * @param diffTreeSource The {@link DiffTreeSource} to be converted
	 * @return The corresponding line graph line
	 */
	String toLabel(final DiffTreeSource diffTreeSource);
	
	/**
     * Converts a line describing a graph (starting with "t # ") in line graph format into a {@link DiffTreeSource}.
	 *
     * @param lineGraphLine A line from a line graph file starting with "t #"
	 * @return The {@link DiffTreeSource} descibed by the label of this line.
	 */
	default DiffTreeSource fromLineGraphLine(final String lineGraphLine) {
		return fromLabel(lineGraphLine.substring((LineGraphConstants.LG_TREE_HEADER + " ").length()));
	}
	
	/**
	 * Prepends the {@link LineGraphConstants#LG_TREE_HEADER tree declaration} to a label and return an entire line graph line.
	 * 
	 * @param diffTreeSource The {@link DiffTreeSource} to be converted
	 * @return The entire line graph line of a {@link DiffTreeSource}.
	 */
    default String toLineGraphLine(final DiffTreeSource diffTreeSource) {
		return LineGraphConstants.LG_TREE_HEADER + " " + toLabel(diffTreeSource);
	}

	default String toLineGraphLineEdgeTyped(final DiffTreeSource diffTreeSource) {
		return LineGraphConstants.LG_ETTREE_HEADER + " " + toLabel(diffTreeSource);
	}
}
