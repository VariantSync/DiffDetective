package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * Reads and writes {@link DiffNode DiffNodes} from and to line graph.
 */
public interface DiffNodeLabelFormat {

	/**
	 * Converts a label of line graph into a {@link DiffNode}.
	 * 
	 * @param lineGraphNodeLabel A string containing the label of the {@link DiffNode}
	 * @param nodeId The id of the {@link DiffNode}
	 * @return The corresponding {@link DiffNode}
	 */
	default DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
	    final DiffNode diffNode = DiffNode.fromID(nodeId);
	    diffNode.setLabel(lineGraphNodeLabel);
	    return diffNode;
	}
	
	/**
	 * Converts a {@link DiffNode} into a {@link DiffNode} label of line graph.
	 * 
	 * @param node The {@link DiffNode} to be converted
	 * @return The corresponding line graph line
	 */
	String writeNodeToLineGraph(final DiffNode node);
}