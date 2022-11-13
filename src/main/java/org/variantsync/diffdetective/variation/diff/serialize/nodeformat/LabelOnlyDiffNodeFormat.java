package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Uses {@link DiffNode#getLabel()} as the linegraph node label.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class LabelOnlyDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.getLabel();
	}
}