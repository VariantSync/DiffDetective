package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for LabelOnly of a node label.
 * Print only the label.
 */
public class LabelOnlyDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
		throw new RuntimeException("Node style ‘LABELONLY’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.getLabel();
	}

}