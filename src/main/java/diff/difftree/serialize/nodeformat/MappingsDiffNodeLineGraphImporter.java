package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Mappings of a node label.
 * Print CodeType and DiffType and Mappings of Macros.
 */
public class MappingsDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLabel, final int nodeId) {
		throw new RuntimeException("Node style ‘MAPPINGS’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, "") + "\"";
	}

}