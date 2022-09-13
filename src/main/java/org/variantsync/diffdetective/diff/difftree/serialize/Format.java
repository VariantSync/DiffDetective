package org.variantsync.diffdetective.diff.difftree.serialize;

import java.util.List;
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
     * This implementation uses {@link forEachUniqueEdge} by calling {@code callback} for each edge
     * in the order given by the lists of {@link forEachUniqueEdge}.
     *
     * @param diffTree to be exported
     * @param callback is called for each edge
     */
    public void forEachEdge(DiffTree diffTree, Consumer<StyledEdge> callback) {
        forEachUniqueEdge(diffTree, (edges) -> {
            for (var edge : edges) {
                callback.accept(edge);
            }
        });
    }

    /**
     * Iterates over all edges in {@code diffTree} and calls {@code callback}, visiting parallel edges only once.
     *
     * Two edges are parallel if they start at the same node and end at the same node. Note that
     * the direction of directed edges matters.
     *
     * All parallel edges are collected into a list and are passed once to {@code callback}.
     *
     * Exporters should use this method to enable subclasses of {@code Format} to filter edges, add
     * new edges and change the order of the exported edges.
     *
     * @param diffTree to be exported
     * @param callback is called for each unique edge
     */
    public void forEachUniqueEdge(DiffTree diffTree, Consumer<List<StyledEdge>> callback) {
        diffTree.forAll((node) -> {
            var beforeParent = node.getBeforeParent();
            var afterParent = node.getAfterParent();

            // Are both parent edges the same?
            if (beforeParent != null && afterParent != null && beforeParent == afterParent) {
                callback.accept(List.of(beforeEdge(node), afterEdge(node)));
            } else {
                if (beforeParent != null) {
                    callback.accept(List.of(beforeEdge(node)));
                }
                if (afterParent != null) {
                    callback.accept(List.of(afterEdge(node)));
                }
            }
        });
    }

    /**
     * Constructs a {@link StyledEdge} from {@code node} and its before parent.
     *
     * The order of these nodes is permuted according to {@link EdgeLabelFormat#getEdgeDirection}
     * of {@link getEdgeFormat()}.
     */
    protected StyledEdge beforeEdge(DiffNode node) {
        return sortedEdgeWithLabel(node, node.getBeforeParent(), StyledEdge.BEFORE);
    }

    /**
     * Constructs a {@link StyledEdge} from {@code node} and its after parent.
     *
     * The order of these nodes is permuted according to {@link EdgeLabelFormat#getEdgeDirection}
     * of {@link getEdgeFormat()}.
     */
    protected StyledEdge afterEdge(DiffNode node) {
        return sortedEdgeWithLabel(node, node.getAfterParent(), StyledEdge.AFTER);
    }

    /**
     * Constructs a {@link StyledEdge} from {@code originalFrom} to {@code originalTo}.
     *
     * The order of these nodes is permuted according to {@link EdgeLabelFormat#getEdgeDirection}
     * of {@link getEdgeFormat()}.
     *
     * @param originalFrom the origin of the constructed edge
     * @param originalTo the destination of the constructed edge
     * @param style the export style of the constructed edge
     * @return a new {@link StyledEdge}
     */
    protected StyledEdge sortedEdgeWithLabel(DiffNode originalFrom, DiffNode originalTo, StyledEdge.Style style) {
        var edge = edgeFormat.getEdgeDirection().sort(originalFrom, originalTo);
        return new StyledEdge(edge.first(), edge.second(), style);
    }
}
