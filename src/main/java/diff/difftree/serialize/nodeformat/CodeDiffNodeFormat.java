package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;

/**
 * A concrete implementation for Code of a node label.
 * Print Node as Code.
 */
public class CodeDiffNodeFormat implements DiffNodeLabelFormat {

	@Override
	public String toLabel(final DiffNode node) {
		return "\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, node.getLabel().trim()) + "\"";
	}

}