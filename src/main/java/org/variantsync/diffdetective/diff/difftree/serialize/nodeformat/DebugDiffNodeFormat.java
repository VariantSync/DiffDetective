package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.util.FileUtils;

/**
 * A concrete implementation for Debug of a node label.
 * Print CodeType and DiffType and Mappings if Macro and Text if Code.
 */
public class DebugDiffNodeFormat implements DiffNodeLabelFormat {
	
	@Override
	public String toLabel(final DiffNode node) {
		return node.diffType + "_" + node.codeType + "_\"" +
				DiffNodeLabelPrettyfier.prettyPrintIfMacroOr(
						node,
						FileUtils.replaceLineEndings(node.getLabel().trim().replaceAll("\t", "  "), "<br>"))
				+ "\"";
	}

}