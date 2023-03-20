package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.List;

import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Replaces the label of the root node with {@code "root"}.
 * @author Benjamin Moosherr
 */
public class RenameRootNodeFormat implements DiffNodeLabelFormat {
    private final DiffNodeLabelFormat inner;

    public RenameRootNodeFormat(DiffNodeLabelFormat inner) {
        this.inner = inner;
    }

    @Override
    public String toLabel(final DiffNode node) {
        if (node.isRoot()) {
            return "root";
        } else {
            return inner.toLabel(node);
        }
    }

    @Override
    public List<String> toMultilineLabel(final DiffNode node) {
        if (node.isRoot()) {
            return List.of("root");
        } else {
            return inner.toMultilineLabel(node);
        }
    }
}
