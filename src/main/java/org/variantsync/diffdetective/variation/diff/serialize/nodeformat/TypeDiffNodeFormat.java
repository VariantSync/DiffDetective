package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Labels are of the form <code>DiffType_NodeType</code>.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class TypeDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType;
	}
}
