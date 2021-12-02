package diff.difftree.serialize.treeformat;

import diff.difftree.DiffTreeSource;
import diff.difftree.LineGraphConstants;

/**
 * Reads and writes {@link DiffTreeSource} from and to line graph.
 */
public interface DiffTreeLabelFormat {

	/**
	 * Converts a label of line graph into a {@link DiffTreeSource}.
	 * 
	 * @param lineGraphNodeLine A string containing the label of the {@link DiffTreeSource}
	 * @return The corresponding {@link DiffTreeSource}
	 */
	public DiffTreeSource readTreeHeaderFromLineGraph(final String lineGraphNodeLine);
	
	/**
	 * Converts a {@link DiffTreeSource} label of line graph.
	 * 
	 * @param node The {@link DiffTreeSource} to be converted
	 * @return The corresponding line graph line
	 */
	public String writeTreeHeaderToLineGraph(final DiffTreeSource diffTreeSource);
	
	/**
	 * Removes the overhead and returns the label of a {@link DiffTreeSoruce} in the line graph format.
	 * 
	 * @param lineGraphNodeLine The tree label to be extracted
	 * @return The tree label
	 */
	public default String extractRawTreeLabel(String lineGraphNodeLine) {
		return lineGraphNodeLine.substring((LineGraphConstants.LG_TREE_HEADER + " ").length());
	}
	
	/**
	 * Prepends the {@link LineGraphConstants#LG_TREE_HEADER tree declaration} to a label and return an entire line graph line.
	 * 
	 * @param lineGraphNodeLine The tree label
	 * @return The entire line graph line of a {@link DiffTreeSource}.
	 */
	public default String setRawTreeLabel(String lineGraphNodeLine) {
		return LineGraphConstants.LG_TREE_HEADER + " " + lineGraphNodeLine;
	}
	
}