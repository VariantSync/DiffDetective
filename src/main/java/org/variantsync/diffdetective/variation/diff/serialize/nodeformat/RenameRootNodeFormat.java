package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.List;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Replaces the label of the root node with {@code "root"}.
 * @author Benjamin Moosherr
 */
public class RenameRootNodeFormat<L extends Label> implements DiffNodeLabelFormat<L> {
    private final String rootLabel;
    private final DiffNodeLabelFormat<L> inner;

    public RenameRootNodeFormat(DiffNodeLabelFormat<L> inner) {
        this(inner, "root");
    }

    public RenameRootNodeFormat(DiffNodeLabelFormat<L> inner, String rootLabel) {
        this.rootLabel = rootLabel;
        this.inner = inner;
    }

    @Override
    public String toLabel(final DiffNode<? extends L> node) {
        if (node.isRoot()) {
            return rootLabel;
        } else {
            return inner.toLabel(node);
        }
    }

    @Override
    public List<String> toMultilineLabel(final DiffNode<? extends L> node) {
        if (node.isRoot()) {
            return List.of("root");
        } else {
            return inner.toMultilineLabel(node);
        }
    }
}
