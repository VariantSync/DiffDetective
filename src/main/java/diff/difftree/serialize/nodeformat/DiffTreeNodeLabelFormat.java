package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * Reads and writes {@link DiffNode DiffNodes} from and to line graph.
 */
public interface DiffTreeNodeLabelFormat {

	/**
	 * Converts a label of line graph into a {@link DiffNode}.
	 * 
	 * @param lineGraphNodeLabel A string containing the label of the {@link DiffNode}
	 * @param nodeId The id of the {@link DiffNode}
	 * @return The corresponding {@link DiffNode}
	 */
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId);
	
	/**
	 * Converts a {@link DiffNode} into a {@link DiffNode} label of line graph.
	 * 
	 * @param node The {@link DiffNode} to be converted
	 * @return The corresponding line graph line
	 */
	public String writeNodeToLineGraph(final DiffNode node);
	
}