package org.variantsync.diffdetective.diff.difftree.serialize.nodeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

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
