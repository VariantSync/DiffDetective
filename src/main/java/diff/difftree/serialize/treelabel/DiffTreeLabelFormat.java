package diff.difftree.serialize.treelabel;

import diff.difftree.DiffTreeSource;

/**
 * Read and write {@link DiffTreeSource} from and to line graph.
 */
public interface DiffTreeLabelFormat {

	/**
	 * Converts an entire line of a line graph into a {@link DiffTreeSource}.
	 * 
	 * @param lineGraphNodeLine A string that should represents a {@link DiffTreeSource}.
	 * @return The corresponding {@link DiffTreeSource}.
	 */
	public DiffTreeSource readTreeHeaderFromLineGraph(final String lineGraphNodeLine);
	
	/**
	 * Converts a {@link DiffTreeSource} into an entire line of a line graph.
	 * 
	 * @param node The {@link DiffTreeSource} to be converted
	 * @return The corresponding line graph line.
	 */
	public String writeTreeHeaderToLineGraph(final DiffTreeSource diffTreeSource);
	
}