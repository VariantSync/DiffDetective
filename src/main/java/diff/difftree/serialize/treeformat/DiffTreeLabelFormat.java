package diff.difftree.serialize.treeformat;

import diff.difftree.DiffTreeSource;

/**
 * Reads and writes {@link DiffTreeSource} from and to line graph.
 */
public interface DiffTreeLabelFormat {

	/**
	 * Converts an entire line of a line graph into a {@link DiffTreeSource}.
	 * 
	 * @param lineGraphNodeLine A string that should represent a {@link DiffTreeSource}
	 * @return The corresponding {@link DiffTreeSource}
	 */
	public DiffTreeSource readTreeHeaderFromLineGraph(final String lineGraphNodeLine);
	
	/**
	 * Converts a {@link DiffTreeSource} into an entire line of a line graph.
	 * 
	 * @param node The {@link DiffTreeSource} to be converted
	 * @return The corresponding line graph line
	 */
	public String writeTreeHeaderToLineGraph(final DiffTreeSource diffTreeSource);
	
}