package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Type of a node label.
 * Print CodeType and DiffType.
 */
public class TypeDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
		throw new RuntimeException("Node style ‘TYPE’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	// TODO write tests for this node label
	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.diffType + "_" + node.codeType;
	}

}