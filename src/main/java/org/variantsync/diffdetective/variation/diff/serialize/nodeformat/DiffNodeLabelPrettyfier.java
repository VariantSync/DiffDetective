package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.prop4j.Node;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.NodeType;

/**
 * Making methods accessible for {@link DiffNodeLabelFormat} for formatting the output of {@link DiffNode DiffNodes}.
 */
public abstract class DiffNodeLabelPrettyfier {
    /**
     * Auxiliary method for {@link DiffNodeLabelPrettyfier#prettyPrintIfAnnotationOr(DiffNode, String)}.
     * Returns a string starting with the nodes {@link DiffNode#getNodeType()}, and its {@link DiffNode#getFormula()}
     * if it has a formula.
     * @param node The {@link DiffNode} to print.
     * @return NodeType and {@link DiffNode#getFormula} of the node in a single string, seperated by a space character.
     */
    private static String prettyPrintTypeAndMapping(final DiffNode<?> node) {
        String result = node.getNodeType().name;
        final Node fm = node.getFormula();
        if (fm != null) {
            result += " " + fm;
        }
        return result;
    }

    /**
     * Invokes {@link #prettyPrintTypeAndMapping(DiffNode)} if the given
     * node {@link NodeType#isAnnotation() is an annotation}, and returns the elseValue otherwise.
     * @param node The {@link DiffNode} to prettyprint.
     * @param elseValue The value to return in case the given node is not an annotation.
     * @return The generated label.
     */
    public static String prettyPrintIfAnnotationOr(final DiffNode<?> node, final String elseValue) {
        String result = "";
        if (node.isAnnotation()) {
            result += prettyPrintTypeAndMapping(node);
        } else {
            result += elseValue;
        }
        return result;
    }
}
