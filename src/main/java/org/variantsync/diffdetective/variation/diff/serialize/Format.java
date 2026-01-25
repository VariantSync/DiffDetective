package org.variantsync.diffdetective.variation.diff.serialize;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.eclipse.jgit.diff.EditList;
import org.eclipse.jgit.diff.Sequence;
import org.eclipse.jgit.diff.SequenceComparator;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.DiffNodeLabelFormat;

import java.util.List;
import java.util.function.Consumer;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Format used for exporting a {@link VariationDiff}.
 * For easy reusability this class is composed of separate node and edge formats.
 *
 * The exported {@link VariationDiff} can be influenced in the following ways:
 * <ul>
 * <li>Providing both a node and an edge label format.
 * <li>Changing the order, filtering or adding the nodes and edges by creating a subclass of
 * {@code Format}.
 * </ul>
 */
public class Format<L extends Label> {
    private final DiffNodeLabelFormat<? super L> nodeFormat;
    private final EdgeLabelFormat<? super L> edgeFormat;

    public Format(DiffNodeLabelFormat<? super L> nodeFormat, EdgeLabelFormat<? super L> edgeFormat) {
        this.nodeFormat = nodeFormat;
        this.edgeFormat = edgeFormat;
    }

    public DiffNodeLabelFormat<? super L> getNodeFormat() {
        return nodeFormat;
    }

    public EdgeLabelFormat<? super L> getEdgeFormat() {
        return edgeFormat;
    }

    /**
     * Iterates over all {@link DiffNode}s in {@code variationDiff} and calls {@code callback}.
     *
     * Exporters should use this method to enable subclasses of {@code Format} to filter nodes, add
     * new nodes and change the order of the exported nodes.
     *
     * This implementation is equivalent to {@link VariationDiff#forAll}.
     *
     * @param variationDiff to be exported
     * @param callback is called for each node
     */
    public <La extends L> void forEachNode(VariationDiff<La> variationDiff, Consumer<DiffNode<La>> callback) {
        variationDiff.forAll(callback);
    }

    /**
     * Iterates over all edges in {@code variationDiff} and calls {@code callback}, visiting
     * parallel edges only once.
     *
     * If an edge is unchanged (there are equal before and after edges) {@code callback} is only
     * called once.
     *
     * Exporters should use this method to enable subclasses of {@code Format} to filter edges, add
     * new edges and change the order of the exported edges.
     *
     * @param variationDiff to be exported
     * @param callback is called for each unique edge
     */
    public <La extends L> void forEachEdge(VariationDiff<La> variationDiff, Consumer<StyledEdge<La>> callback) {
        variationDiff.forAll((node) -> {
            List<DiffNode<La>> beforeChildren = node.getChildOrder(BEFORE);
            List<DiffNode<La>> afterChildren = node.getChildOrder(AFTER);

            class ListSequence<E> extends Sequence {
                public final List<E> list;

                public ListSequence(List<E> list) {
                    this.list = list;
                }

                @Override
                public int size() {
                    return list.size();
                }
            }

            class ListSequenceComparator<E> extends SequenceComparator<ListSequence<E>> {
                @Override
                public boolean equals(ListSequence<E> sequence1, int index1, ListSequence<E> sequence2, int index2) {
                    return sequence1.list.get(index1) == sequence2.list.get(index2);
                }

                @Override
                public int hash(ListSequence<E> sequence, int index) {
                    return System.identityHashCode(sequence.list.get(index));
                }
            }

            final EditList editList = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS).diff(
                new ListSequenceComparator<DiffNode<La>>(),
                new ListSequence<>(beforeChildren),
                new ListSequence<>(afterChildren)
            );

            int beforeIndex = 0;
            for (var edit : editList) {
                for (; beforeIndex < edit.getBeginA(); ++beforeIndex) {
                    sortedEdgeWithLabel(beforeChildren.get(beforeIndex), node, StyledEdge.ALWAYS, callback);
                }
                for (; beforeIndex < edit.getEndA(); ++beforeIndex) {
                    sortedEdgeWithLabel(beforeChildren.get(beforeIndex), node, StyledEdge.BEFORE, callback);
                }
                for (int afterIndex = edit.getBeginB(); afterIndex < edit.getEndB(); ++afterIndex) {
                    sortedEdgeWithLabel(afterChildren.get(afterIndex), node, StyledEdge.AFTER, callback);
                }
            }
            for (; beforeIndex < beforeChildren.size(); ++beforeIndex) {
                sortedEdgeWithLabel(beforeChildren.get(beforeIndex), node, StyledEdge.ALWAYS, callback);
            }
        });
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
     * @param callback the consumer which is called with the resulting {@link StyledEdge}
     */
    protected <La extends L> void sortedEdgeWithLabel(
            DiffNode<La> originalFrom,
            DiffNode<La> originalTo,
            StyledEdge.Style style,
            Consumer<StyledEdge<La>> callback
    ) {
        var edge = edgeFormat.getEdgeDirection().sort(originalFrom, originalTo);
        callback.accept(new StyledEdge<>(edge.first(), edge.second(), style));
    }
}
