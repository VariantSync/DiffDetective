package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Print NodeType, DiffType and Mappings for Annotations and Text for Artifacts.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class DebugDiffNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
	@Override
	public String toLabel(final DiffNode<? extends L> node) {
		return node.diffType + "_" + node.nodeType + "_\"" +
				DiffNodeLabelPrettyfier.prettyPrintIfAnnotationOr(
						node,
						FileUtils.replaceLineEndings(node.getLabel().toString().trim().replaceAll("\t", "  "), "<br>"))
				+ "\"";
	}
}
