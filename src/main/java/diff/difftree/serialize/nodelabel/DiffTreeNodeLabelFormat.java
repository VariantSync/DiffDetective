package diff.difftree.serialize.nodelabel;

import diff.difftree.DiffNode;

/**
 * Read and write {@link DiffNode DiffNodes} from and to line graph.
 */
public interface DiffTreeNodeLabelFormat {

	/**
	 * Converts an entire line of a line graph into a {@link DiffNode}.
	 * 
	 * @param lineGraphNodeLine A string that should represents a {@link DiffNode}.
	 * @return The corresponding {@link DiffNode}.
	 */
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine);
	
	/**
	 * Converts a {@link DiffNode} into an entire line of a line graph.
	 * 
	 * @param node The {@link DiffNode} to be converted
	 * @return The corresponding line graph line.
	 */
	public String writeNodeToLineGraph(final DiffNode node);
	
}
