package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.List;

import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Replaces the label of the root node with {@code "root"}.
 * @author Benjamin Moosherr
 */
public class RenameRootNodeFormat implements DiffNodeLabelFormat {
    private final String rootLabel;
    private final DiffNodeLabelFormat inner;

    public RenameRootNodeFormat(DiffNodeLabelFormat inner) {
        this(inner, "root");
    }

    public RenameRootNodeFormat(DiffNodeLabelFormat inner, String rootLabel) {
        this.rootLabel = rootLabel;
        this.inner = inner;
    }

    @Override
    public String toLabel(final DiffNode node) {
        if (node.isRoot()) {
            return rootLabel;
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
