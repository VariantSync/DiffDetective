package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Debug of a node label.
 * Print CodeType and DiffType and Mappings if Macro and Text if Code.
 */
public class DebugDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
		throw new RuntimeException("Node style ‘DEBUG’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, node.getLabel().trim()) + "\"";
	}

}