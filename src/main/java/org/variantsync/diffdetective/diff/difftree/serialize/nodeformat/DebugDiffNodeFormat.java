package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.util.FileUtils;

/**
 * Print NodeType and DiffType and Mappings if Macro and Text if Code.
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class DebugDiffNodeFormat implements DiffNodeLabelFormat {
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.nodeType + "_\"" +
				DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(
						node,
						FileUtils.replaceLineEndings(node.getLabel().trim().replaceAll("\t", "  "), "<br>"))
				+ "\"";
	}
}
