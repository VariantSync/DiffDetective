package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Labels are of the form <code>DiffType_NodeType</code>.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class TypeDiffNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
	@Override
	public String toLabel(final DiffNode<? extends L> node) {
		return node.diffType + "_" + node.nodeType;
	}
}
