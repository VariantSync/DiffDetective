package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import org.prop4j.NodeWriter;
import org.variantsync.diffdetective.util.LaTeX;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.diffdetective.variation.diff.DiffNode;

import java.util.Arrays;
import java.util.List;

public class PaperNodeFormat implements DiffNodeLabelFormat {
    public static final int MAX_ARTIFACT_LABEL_LENGTH = 12;

    @Override
    public String toLabel(DiffNode node) {
        if (node.isRoot()) {
            return "r";
        }

        String s = "";

        if (node.isAnnotation()) {
//            if (node.getNodeType() == NodeType.ELSE) {
//                s += "ELSE";
//            } else {
              s += node.getFormula().toString(NodeWriter.logicalSymbols);
//            }
        } else {
            final int lineNoFrom = node.getFromLine().inDiff();
            final int lineNoTo   = node.getToLine().inDiff();
            s += lineNoFrom;
            if (lineNoFrom != lineNoTo - 1) {
                s += "-" + lineNoTo;
            }

            s += ": ";


            String label = node.getLabel().toString().trim();
            if (label.length() > MAX_ARTIFACT_LABEL_LENGTH) {
                label = StringUtils.clamp(MAX_ARTIFACT_LABEL_LENGTH - 3, label) + "...";
            }
            s += label; //LaTeX.escape(label);
        }

        return s;
    }

    @Override
    public List<String> toMultilineLabel(DiffNode node) {
        return Arrays.stream(toLabel(node).split(StringUtils.LINEBREAK)).toList();
    }
}
