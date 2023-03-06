package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;

import java.awt.*;

public class DiffNodeView extends NodeView<DiffNode> {
    private final DiffNodeLabelFormat nodeLabelFormat;
    public DiffNodeView(DiffNode node, DiffNodeLabelFormat nodeLabelFormat) {
        super(node);
        this.nodeLabelFormat = nodeLabelFormat;
    }

    @Override
    public Color borderColor() {
        return Colors.ofNodeType(node.getNodeType());
    }

    @Override
    public Color nodeColor() {
        return Colors.ofDiffType.get(node.getDiffType());
    }

    @Override
    public String nodeLabel() {
        return nodeLabelFormat.toLabel(node);
    }
}
