package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;

import java.util.function.Consumer;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Format used for exporting a {@link DiffTree}.
 * For easy reusability this class is composed of separate node and edge formats.
 *
 * The exported {@link DiffTree} can be influenced in the following ways:
 * <ul>
 * <li>Providing both a node and an edge label format.
 * <li>Changing the order, filtering or adding the nodes and edges by creating a subclass of
 * {@code Format}.
 * </ul>
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
    public void forEachEdge(DiffTree diffTree, Consumer<StyledEdge> callback) {
        diffTree.forAll((node) -> {
            var beforeParent = node.getParent(BEFORE);
            var afterParent = node.getParent(AFTER);

            // Are both parent edges the same?
            if (beforeParent != null && beforeParent == afterParent) {
                callback.accept(beforeAndAfterEdge(node));
            } else {
                if (beforeParent != null) {
                    callback.accept(beforeEdge(node));
                }
                if (afterParent != null) {
                    callback.accept(afterEdge(node));
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
        return sortedEdgeWithLabel(node, node.getParent(BEFORE), StyledEdge.BEFORE);
    }

    /**
     * Constructs a {@link StyledEdge} from {@code node} and its after parent.
     *
     * The order of these nodes is permuted according to {@link EdgeLabelFormat#getEdgeDirection}
     * of {@link getEdgeFormat()}.
     */
    protected StyledEdge afterEdge(DiffNode node) {
        return sortedEdgeWithLabel(node, node.getParent(AFTER), StyledEdge.AFTER);
    }


    /**
     * Constructs a {@link StyledEdge} from {@code node} and its before and after parent.
     * The before and after parent are assumed to be equal.
     *
     * The order of these nodes is permuted according to {@link EdgeLabelFormat#getEdgeDirection}
     * of {@link getEdgeFormat()}.
     */
    protected StyledEdge beforeAndAfterEdge(DiffNode node) {
        Assert.assertTrue(node.getParent(BEFORE) != null && node.getParent(BEFORE) == node.getParent(AFTER));
        return sortedEdgeWithLabel(node, node.getParent(BEFORE), StyledEdge.ALWAYS);
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
