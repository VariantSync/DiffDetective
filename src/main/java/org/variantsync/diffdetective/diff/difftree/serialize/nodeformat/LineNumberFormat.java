package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Labels nodes using their line number in the source diff.
 */
public class LineNumberFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return String.valueOf(node.getFromLine().inDiff());
	}
}
