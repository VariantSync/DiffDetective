package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.util.FileUtils;

/**
 * Print NodeType, DiffType and Mappings for Annotations and Text for Artifacts.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class DebugDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType + "_\"" +
				DiffNodeLabelPrettyfier.prettyPrintIfAnnotationOr(
						node,
						FileUtils.replaceLineEndings(node.getLabel().trim().replaceAll("\t", "  "), "<br>"))
				+ "\"";
	}
}
