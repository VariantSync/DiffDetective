package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.function.Function;

public class ShowNodeFormat implements DiffNodeLabelFormat {
    public static String toLabel(DiffNode node, Function<DiffNode, String> artifactPrinter) {
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
            s += artifactPrinter.apply(node);
        }

        return s;
    }

    @Override
    public String toLabel(DiffNode node) {
        return toLabel(
                node,
                n -> StringUtils.clamp(10, n.getLabel().toString().trim())
        );
    }
}
