package org.variantsync.diffdetective.diff.difftree;

import java.util.Collection;

/**
 * Generalisation of DiffTrees to arbitrary change graphs with variability information.
 * The DiffGraph class currently does not model a graph itself but rather
 * turns a given graph into a DiffTree by equipping it with a synthetic root.
 */
public final class DiffGraph {
    private static final String DIFFGRAPH_LABEL = "DiffGraph";

    private DiffGraph() {}

    /**
     * Invokes {@link DiffGraph#fromNodes(Collection, DiffTreeSource)} )} with an unknown DiffTreeSource.
     */
    public static DiffTree fromNodes(final Collection<DiffNode> nodes) {
        return fromNodes(nodes, DiffTreeSource.Unknown);
    }

    /**
     * Takes a set of DiffNodes that forms a DiffGraph (i.e., similar to a DiffTree but with no explicit root)
     * and converts it to a DiffTree by equipping it with a synthetic root node.
     * @param nodes a DiffGraph
     * @param source the source where the DiffGraph came from.
     * @return A DiffTree representing the DiffGraph with a synthetic root node.
     */
    public static DiffTree fromNodes(final Collection<DiffNode> nodes, final DiffTreeSource source) {
        final DiffNode newRoot = DiffNode.createRoot();
        newRoot.setLabel(DIFFGRAPH_LABEL);
        nodes.stream()
                .filter(DiffNode::isRoot)
                .forEach(n ->
                        n.diffType.matchBeforeAfter(
                                () -> newRoot.addBeforeChild(n),
                                () -> newRoot.addAfterChild(n)
                        ));
        return new DiffTree(newRoot, source);
    }
}
