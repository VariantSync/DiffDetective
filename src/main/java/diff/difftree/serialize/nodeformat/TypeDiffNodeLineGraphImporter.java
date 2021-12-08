package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Type of a node label.
 * Print CodeType and DiffType.
 */
public class TypeDiffNodeLineGraphImporter implements DiffTreeNodeLabelFormat {

	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.diffType + "_" + node.codeType;
	}

}