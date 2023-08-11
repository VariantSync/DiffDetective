package org.variantsync.diffdetective.variation.diff;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;

import java.util.Collection;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Generalisation of VariationDiffs to arbitrary change graphs with variability information.
 * The DiffGraph class currently does not model a graph itself but rather
 * turns a given graph into a VariationDiff by equipping it with a synthetic root.
 */
@Deprecated
public final class DiffGraph {
    private static final String DIFFGRAPH_LABEL = "DiffGraph";

    private DiffGraph() {}

    /**
     * Invokes {@link DiffGraph#fromNodes(Collection, VariationDiffSource)} )} with an unknown VariationDiffSource.
     */
    public static VariationDiff<DiffLinesLabel> fromNodes(final Collection<DiffNode<DiffLinesLabel>> nodes) {
        return fromNodes(nodes, VariationDiffSource.Unknown);
    }

    /**
     * Takes a set of DiffNodes that forms a DiffGraph (i.e., similar to a VariationDiff but with no explicit root)
     * and converts it to a VariationDiff by equipping it with a synthetic root node.
     * @param nodes a DiffGraph
     * @param source the source where the DiffGraph came from.
     * @return A VariationDiff representing the DiffGraph with a synthetic root node.
     */
    public static VariationDiff<DiffLinesLabel> fromNodes(final Collection<DiffNode<DiffLinesLabel>> nodes, final VariationDiffSource source) {
        final DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(DiffLinesLabel.ofCodeBlock(DIFFGRAPH_LABEL));
        nodes.stream()
                .filter(DiffNode::isRoot)
                .forEach(n ->
                        n.diffType.forAllTimesOfExistence(
                                () -> newRoot.addChild(n, BEFORE),
                                () -> newRoot.addChild(n, AFTER)
                        ));
        return new VariationDiff<>(newRoot, source);
    }
}
