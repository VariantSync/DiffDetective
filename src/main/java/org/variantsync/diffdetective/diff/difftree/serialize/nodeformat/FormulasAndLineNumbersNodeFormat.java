package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

/**
 * Produces labels of kind <code>fromLine-toLine: CodeType</code>, suffixed by the node's formula if it is a macro.
 * The line numbers reference the line numbers in the diff.
 * @see DiffNode#getFromLine()
 * @see DiffNode#getToLine()
 * @see org.variantsync.diffdetective.diff.DiffLineNumber#inDiff
 * @author Paul Bittner, Kevin Jedelhauser
 */
public class FormulasAndLineNumbersNodeFormat implements DiffNodeLabelFormat {
    @Override
    public String toLabel(DiffNode node) {
        final String lineNumbers = node.getFromLine().inDiff + "-" + node.getToLine().inDiff + ": " + node.codeType;
        if (node.isMacro()) {
            return lineNumbers + " " + node.getDirectFeatureMapping();
        }
        return lineNumbers;
    }
}
