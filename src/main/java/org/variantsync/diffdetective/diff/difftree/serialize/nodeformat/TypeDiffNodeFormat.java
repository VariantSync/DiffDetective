package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Labels are of the form <code>NodeType</code>.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class TypeDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType;
	}
}
