package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;

public class ShowNodeFormat implements DiffNodeLabelFormat {
    @Override
    public String toLabel(DiffNode node) {
        if (node.isRoot()) {
            return "⊤";
        }

        String s = "";//node.getFromLine().inDiff() + " ";

        if (node.isAnnotation()) {
            s += node.getNodeType();
            if (node.getNodeType() != NodeType.ELSE) {
                s += " " + node.getFormula().toString(NodeWriter.logicalSymbols);
            }
        } else {
            s += StringUtils.clamp(10, node.getLabel().trim());
        }

        return s;
    }
}
