package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Print CodeType and DiffType and Mappings of Macros.
 * The produced label will be <code>CodeType_DiffType_"macro formula"</code> for mapping nodes,
 * and <code>CodeType_DiffType_""</code> for non-mapping nodes.
 * @see DiffNodeLabelPrettyfier#prettyPrintIfMacroOr(DiffNode, String)
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class MappingsDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, "") + "\"";
	}
}