package diff.difftree.serialize.diffnodestyle;

import diff.difftree.DiffNode;
import org.prop4j.Node;

/**
 * Making methods accessible for DiffNodeLineGraphImporters for formatting the output of {@link DiffNode DiffNodes}.
 */
abstract class DiffNodeLabelPrettyfier {

    protected static String prettyPrintTypeAndMapping(final DiffNode node) {
        String result = node.codeType.name;
        final Node fm = node.getDirectFeatureMapping();
        if (fm != null) {
            result += " " + fm;
        }
        return result;
    }

    protected static String prettyPrintIfMacroOr(final DiffNode node, final String elseValue) {
        String result = "";
        if (node.codeType.isMacro()) {
            result += prettyPrintTypeAndMapping(node);
        } else {
            result += elseValue;
        }
        return result;
    }
}
