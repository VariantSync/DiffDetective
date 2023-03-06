package org.variantsync.diffdetective.show.variation;

import java.awt.*;

public abstract class NodeView<N> {
    protected final N node;

    protected NodeView(N node) {
        this.node = node;
    }

    public abstract Color borderColor();

    public abstract Color nodeColor();

    public abstract String nodeLabel();
}
