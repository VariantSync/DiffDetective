package diff.difftree.serialize.nodelabel;

import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for Mappings of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print CodeType and DiffType and Mappings of Macros.
 */
public class MappingsDiffNodeLineGraphImporter extends DiffNodeLabelPrettyfier implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
		throw new RuntimeException("Node style ‘MAPPINGS’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + node.diffType + "_" + node.codeType + "_\"" + prettyPrintIfMacroOr(node, "") + "\"";
	}

}