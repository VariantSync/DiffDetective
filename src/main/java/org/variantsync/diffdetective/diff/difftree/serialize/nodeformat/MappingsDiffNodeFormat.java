package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * A concrete implementation for Mappings of a node label.
 * Print CodeType and DiffType and Mappings of Macros.
 */
public class MappingsDiffNodeFormat implements DiffNodeLabelFormat {

	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" + DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(node, "") + "\"";
	}

}