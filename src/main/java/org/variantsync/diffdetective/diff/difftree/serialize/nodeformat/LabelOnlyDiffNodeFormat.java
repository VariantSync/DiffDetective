package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

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