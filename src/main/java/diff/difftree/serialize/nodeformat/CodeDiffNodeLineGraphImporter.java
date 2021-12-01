package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Code of a node label.
 * Print Node as Code.
 */
public class CodeDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
		throw new RuntimeException("Node style ‘CODE’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return "\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, node.getLabel().trim()) + "\"";
	}

}