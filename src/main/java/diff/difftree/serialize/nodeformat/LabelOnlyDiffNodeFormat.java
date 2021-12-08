package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for LabelOnly of a node label.
 * Print only the label.
 */
public class LabelOnlyDiffNodeFormat implements DiffTreeNodeLabelFormat {
	
	@Override
	public String writeNodeToLineGraph(final DiffNode node) {
		return node.getLabel();
	}

}