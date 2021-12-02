package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Mappings of a node label.
 * Print CodeType and DiffType and Mappings of Macros.
 */
public class MappingsDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, "") + "\"";
	}

}