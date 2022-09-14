package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Print NodeType and DiffType and Mappings of Macros.
 * The produced label will be <code>NodeType"macro formula"</code> for mapping nodes,
 * and <code>NodeType""</code> for non-mapping nodes.
 * @see DiffNodeLabelPrettyfier#prettyPrintIfMacroOr(DiffNode, String)
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class MappingsDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, "") + "\"";
	}
}
