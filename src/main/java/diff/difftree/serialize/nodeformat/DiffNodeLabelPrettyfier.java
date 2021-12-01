package diff.difftree.serialize.nodeformat;

import diff.difftree.DiffNode;
import org.prop4j.Node;

/**
 * Making methods accessible for DiffNodeLineGraphImporters for formatting the output of {@link DiffNode DiffNodes}.
 */
public abstract class DiffNodeLabelPrettyfier {

	/**
	 * Auxiliary method for {@link DiffNodeLabelPrettyfier#prettyPrintIfMacroOr(DiffNode, String)}.
	 * 
	 * @param node The {@link DiffNode}
	 * @return
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
     * @param elseValue
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