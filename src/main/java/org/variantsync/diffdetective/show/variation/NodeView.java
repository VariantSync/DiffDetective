package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;

import java.awt.*;

public class NodeView {
    protected final DiffNode node;
    private DiffNodeLabelFormat labelFormat;

    protected NodeView(DiffNode node, DiffNodeLabelFormat format) {
        this.node = node;
        this.labelFormat = format;
    }

    public Color borderColor() {
        return Colors.ofNodeType(node.getNodeType());
    }

    public Color nodeColor() {
        return Colors.ofDiffType.get(node.getDiffType());
    }

    public String nodeLabel() {
        return labelFormat.toLabel(node);
    }

    public DiffNodeLabelFormat getLabelFormat() {
        return labelFormat;
    }

    public void setLabelFormat(DiffNodeLabelFormat labelFormat) {
        this.labelFormat = labelFormat;
    }
}
