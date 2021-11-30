package diff.difftree.serialize.diffnodestyle;

import diff.difftree.DiffNode;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;

/**
 * A concrete implementation for Type of a {@link DiffTreeLineGraphImportOptions.NodeStyle}.
 * Print CodeType and DiffType.
 */
public class TypeDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public DiffNode readNodeFromLineGraph(final String lineGraphNodeLine) {
		throw new RuntimeException("Node style ‘TYPE’ is not supported to be read from. Too less information given to restore DiffNode.");
	}

	@Override
	public String writeNodetoLineGraph(final DiffNode node) {
		return "v " + node.getID() + " " + node.diffType + "_" + node.codeType;
	}

}