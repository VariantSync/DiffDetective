package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Uses {@link DiffNode#getLabel()} as the linegraph node label.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class LabelOnlyDiffNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
	@Override
	public String toLabel(final DiffNode<? extends L> node) {
		return node.getLabel().toString();
	}
}
