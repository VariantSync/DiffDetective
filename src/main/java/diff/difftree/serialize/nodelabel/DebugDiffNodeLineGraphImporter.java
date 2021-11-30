package diff.difftree.serialize.nodelabel;

import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for Debug of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print CodeType and DiffType and Mappings if Macro and Text if Code.
 */
public class DebugDiffNodeLineGraphImporter extends DiffNodeLabelPrettyfier implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
		throw new RuntimeException("Node style ‘DEBUG’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + node.diffType + "_" + node.codeType + "_\"" + prettyPrintIfMacroOr(node, node.getLabel().trim()) + "\"";
	}

}