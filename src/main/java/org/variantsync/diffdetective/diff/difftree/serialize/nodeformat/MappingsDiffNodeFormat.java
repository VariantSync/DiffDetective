package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Print NodeType and DiffType and Mappings of Annotations.
 * The produced label will be <code>DiffType_NodeType_"annotation formula"</code> for mapping nodes,
 * and <code>DiffType_NodeType_""</code> for non-mapping nodes.
 * @see DiffNodeLabelPrettyfier#prettyPrintIfAnnotationOr(DiffNode, String)
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class MappingsDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfAnnotationOr(node, "") + "\"";
	}
}
