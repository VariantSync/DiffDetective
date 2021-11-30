package diff.difftree.serialize.diffnodestyle;

import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for Code of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print Node as Code.
 */
public class CodeDiffNodeLineGraphImporter extends DiffNodeLabelPrettyfier implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
		throw new RuntimeException("Node style ‘CODE’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodetoLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + "\"" + prettyPrintIfMacroOr(node, node.getLabel().trim()) + "\"";
	}

}
