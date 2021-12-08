package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Type of a node label.
 * Print CodeType and DiffType.
 */
public class TypeDiffNodeFormat implements DiffNodeLabelFormat {

	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.codeType;
	}

}