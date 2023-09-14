package org.variantsync.diffdetective.show.variation;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;

import java.awt.*;

public class NodeView<L extends Label> {
    protected final DiffNode<L> node;
    private VariationDiffApp<L> formatHolder;

    protected NodeView(DiffNode<L> node, VariationDiffApp<L> formatHolder) {
        this.node = node;
        this.formatHolder = formatHolder;
    }

    public Color borderColor() {
        return Colors.ofNodeType(node.getNodeType());
    }

    public Color nodeColor() {
        return Colors.ofDiffType.get(node.getDiffType());
    }

    public String nodeLabel() {
        return getLabelFormat().toLabel(node);
    }

    public DiffNodeLabelFormat<? super L> getLabelFormat() {
        return formatHolder.getLabelFormat();
    }
}
