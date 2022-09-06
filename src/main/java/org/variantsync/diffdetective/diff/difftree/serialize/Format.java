package org.variantsync.diffdetective.diff.difftree.serialize;

import java.util.function.Consumer;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.DiffNodeLabelFormat;

/**
 * Format used for exporting a {@link DiffTree}.
 * For easy reusability this class is composed of separate node and edge formats.
 *
 * The exported {@link DiffTree} can be influenced in the following ways:
 * - Providing both a node and an edge label format.
 * - Changing the order, filtering or adding the nodes and edges by creating a subclass of {@code
 *   Format}.
 */
public class Format {
    private final DiffNodeLabelFormat nodeFormat;
    private final EdgeLabelFormat edgeFormat;

    public Format(DiffNodeLabelFormat nodeFormat, EdgeLabelFormat edgeFormat) {
        this.nodeFormat = nodeFormat;
        this.edgeFormat = edgeFormat;
    }

    public DiffNodeLabelFormat getNodeFormat() {
        return nodeFormat;
    }

    public EdgeLabelFormat getEdgeFormat() {
        return edgeFormat;
    }

    /**
     * Iterates over all {@link DiffNode}s in {@code diffTree} and calls {@code callback}.
     *
     * Exporters should use this method to enable subclasses of {@code Format} to filter nodes, add
     * new nodes and change the order of the exported nodes.
     *
     * This implementation is equivalent to {@link DiffTree#forAll}.
     *
     * @param diffTree to be exported
     * @param callback is called for each node
     */
    public void forEachNode(DiffTree diffTree, Consumer<DiffNode> callback) {
        diffTree.forAll(callback);
    }

    /**
     * Iterates over all edges in {@code diffTree} and calls {@code callback}.
     *
     * Exporters should use this method to enable subclasses of {@code Format} to filter edges, add
     * new edges and change the order of the exported edges.
     *
     * @param diffTree to be exported
     * @param callback is called for each edge
     */
    public void forEachEdge(DiffTree diffTree, Consumer<StyledEdge> callback) {
        diffTree.forAll((node) -> {
            processEdge(node, node.getBeforeParent(), StyledEdge.BEFORE, callback);
            processEdge(node, node.getAfterParent(), StyledEdge.AFTER, callback);
        });
    }

    private void processEdge(DiffNode node, DiffNode parent, StyledEdge.Style style, Consumer<StyledEdge> callback) {
        if (parent == null) {
            return;
        }

        var edge = edgeFormat.getEdgeDirection().sort(node, parent);
        callback.accept(new StyledEdge(
            edge.first(),
            edge.second(),
            style));
    }
}
