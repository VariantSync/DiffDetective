package org.variantsync.diffdetective.variation.diff;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;

import java.util.Collection;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Generalisation of DiffTrees to arbitrary change graphs with variability information.
 * The DiffGraph class currently does not model a graph itself but rather
 * turns a given graph into a DiffTree by equipping it with a synthetic root.
 */
@Deprecated
public final class DiffGraph {
    private static final String DIFFGRAPH_LABEL = "DiffGraph";

    private DiffGraph() {}

    /**
     * Invokes {@link DiffGraph#fromNodes(Collection, DiffTreeSource)} )} with an unknown DiffTreeSource.
     */
    public static DiffTree<DiffLinesLabel> fromNodes(final Collection<DiffNode<DiffLinesLabel>> nodes) {
        return fromNodes(nodes, DiffTreeSource.Unknown);
    }

    /**
     * Takes a set of DiffNodes that forms a DiffGraph (i.e., similar to a DiffTree but with no explicit root)
     * and converts it to a DiffTree by equipping it with a synthetic root node.
     * @param nodes a DiffGraph
     * @param source the source where the DiffGraph came from.
     * @return A DiffTree representing the DiffGraph with a synthetic root node.
     */
    public static DiffTree<DiffLinesLabel> fromNodes(final Collection<DiffNode<DiffLinesLabel>> nodes, final DiffTreeSource source) {
        final DiffNode<DiffLinesLabel> newRoot = DiffNode.createRoot(DiffLinesLabel.ofCodeBlock(DIFFGRAPH_LABEL));
        nodes.stream()
                .filter(DiffNode::isRoot)
                .forEach(n ->
                        n.diffType.forAllTimesOfExistence(
                                () -> newRoot.addChild(n, BEFORE),
                                () -> newRoot.addChild(n, AFTER)
                        ));
        return new DiffTree<>(newRoot, source);
    }
}
