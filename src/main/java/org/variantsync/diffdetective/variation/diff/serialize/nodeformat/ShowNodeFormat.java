package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.function.Function;

public class ShowNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
    public static <L extends Label> String toLabel(DiffNode<L> node, Function<DiffNode<L>, String> artifactPrinter) {
        if (node.isRoot()) {
            return "r";
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
    public String toLabel(DiffNode<? extends L> node) {
        return toLabel(
                node,
                n -> StringUtils.clamp(10, n.getLabel().toString().trim())
        );
    }
}
