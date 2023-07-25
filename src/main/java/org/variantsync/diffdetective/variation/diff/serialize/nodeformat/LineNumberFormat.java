package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Labels nodes using their line number in the source diff.
 */
public class LineNumberFormat<L extends Label> implements DiffNodeLabelFormat<L> {
	@Override
	public String toLabel(final DiffNode<? extends L> node) {
		return String.valueOf(node.getFromLine().inDiff());
	}
}
