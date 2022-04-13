package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Making methods accessible for DiffNodeLineGraphImporters for formatting the output of {@link DiffNode DiffNodes}.
 */
public abstract class DiffNodeLabelPrettyfier {

	/**
	 * Auxiliary method for {@link DiffNodeLabelPrettyfier#prettyPrintIfMacroOr(DiffNode, String)}.
	 * 
	 * @param node The {@link DiffNode}
	 * @return Name and {@link DiffNode::getDirectFeatureMapping} of the node.
	 */
    private static String prettyPrintTypeAndMapping(final DiffNode node) {
        String result = node.codeType.name;
        final Node fm = node.getDirectFeatureMapping();
        if (fm != null) {
            result += " " + fm;
        }
        return result;
    }

    /**
     * Generates a specific label for {@link DiffNode DiffNodes} in line graph.
     * 
     * @param node The {@link DiffNode}
     * @param elseValue The value tu return in case the given node is not a macro.
     * @return The generated label
     */
    public static String prettyPrintIfMacroOr(final DiffNode node, final String elseValue) {
        String result = "";
        if (node.codeType.isMacro()) {
            result += prettyPrintTypeAndMapping(node);
        } else {
            result += elseValue;
        }
        return result;
    }
}