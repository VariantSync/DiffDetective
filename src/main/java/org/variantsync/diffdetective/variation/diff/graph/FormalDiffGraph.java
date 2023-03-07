package org.variantsync.diffdetective.variation.diff.graph;

import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;

import java.util.*;

/**
 * A view on a DiffTree that shows the DiffTree as a list of nodes and edges.
 * The view invalidates as soon as the viewed DiffTree is altered as the view
 * will not update itself.
 *
 * @param nodes The set of all nodes in a DiffTree.
 * @param edges The set of all edges in a DiffTree.
 *
 * @author Paul Bittner
 */
public record FormalDiffGraph(
        Set<DiffNode> nodes,
        Set<Edge> edges
) {
    public record Edge (DiffNode child, DiffNode parent, Time time) {
    }

    /**
     * Creates a GraphView for a given VariationDiff.
     * The produced view reflects the state of the given DiffTree as is.
     * This means, the view is invalid as soon as the given DiffTree gets
     * modified elsewhere.
     * @param d The DiffTree to view as a list of nodes and edges.
     * @return the graph view
     */
    public static FormalDiffGraph fromDiffTree(final DiffTree d) {
        final Set<DiffNode> nodes = new HashSet<>();
        final Set<Edge> edges = new HashSet<>();

        d.forAll(n -> {
           nodes.add(n);
           if (!n.isRoot()) {
               n.getDiffType().forAllTimesOfExistence(
                       time -> edges.add(new Edge(n, n.getParent(time), time))
               );
           }
        });

        return new FormalDiffGraph(nodes, edges);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();

        for (final DiffNode v : nodes) {
            b.append(v.getID()).append(": ").append(v).append(StringUtils.LINEBREAK);
        }

        for (final Edge e : edges) {
            b
                    .append(e.child().getID())
                    .append(" --")
                    .append(e.time())
                    .append("--> ")
                    .append(e.parent().getID())
                    .append(StringUtils.LINEBREAK);
        }

        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final FormalDiffGraph graph = (FormalDiffGraph) o;

        return     nodes.equals(graph.nodes)
                && edges.equals(graph.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }
}
