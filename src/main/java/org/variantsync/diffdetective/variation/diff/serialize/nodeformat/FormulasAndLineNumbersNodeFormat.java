package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.variantsync.diffdetective.diff.text.DiffLineNumber; // For Javadoc
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Produces labels of kind <code>fromLine-toLine: NodeType</code>, suffixed by the node's formula if it is an annotation.
 * The line numbers reference the line numbers in the diff.
 * @see DiffNode#getFromLine()
 * @see DiffNode#getToLine()
 * @see DiffLineNumber#inDiff
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class FormulasAndLineNumbersNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
    @Override
    public String toLabel(DiffNode<? extends L> node) {
        final String lineNumbers = node.getFromLine().inDiff() + "-" + node.getToLine().inDiff() + ": " + node.nodeType;
        if (node.isAnnotation()) {
            return lineNumbers + " " + node.getFormula();
        }
        return lineNumbers;
    }
}
