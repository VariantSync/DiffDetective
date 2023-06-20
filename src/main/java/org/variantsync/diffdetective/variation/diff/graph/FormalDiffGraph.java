package org.variantsync.diffdetective.variation.diff.graph;

import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.Time;

import java.util.*;

/**
 * A view on a VariationDiff that shows the VariationDiff as a list of nodes and edges.
 * The view invalidates as soon as the viewed VariationDiff is altered as the view
 * will not update itself.
 *
 * @param nodes The set of all nodes in a VariationDiff.
 * @param edges The set of all edges in a VariationDiff.
 *
 * @author Paul Bittner
 */
public record FormalDiffGraph<L extends Label>(
        Set<DiffNode<L>> nodes,
        Set<Edge<L>> edges
) {
    public record Edge<L extends Label>(DiffNode<L> child, DiffNode<L> parent, Time time) {
    }

    /**
     * Creates a GraphView for a given VariationDiff.
     * The produced view reflects the state of the given VariationDiff as is.
     * This means, the view is invalid as soon as the given VariationDiff gets
     * modified elsewhere.
     * @param d The VariationDiff to view as a list of nodes and edges.
     * @return the graph view
     */
    public static <L extends Label> FormalDiffGraph<L> fromVariationDiff(final VariationDiff<L> d) {
        final Set<DiffNode<L>> nodes = new HashSet<>();
        final Set<Edge<L>> edges = new HashSet<>();

        d.forAll(n -> {
           nodes.add(n);
           if (!n.isRoot()) {
               n.getDiffType().forAllTimesOfExistence(
                       time -> edges.add(new Edge<>(n, n.getParent(time), time))
               );
           }
        });

        return new FormalDiffGraph<>(nodes, edges);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();

        for (final DiffNode<L> v : nodes) {
            b.append(v.getID()).append(": ").append(v).append(StringUtils.LINEBREAK);
        }

        for (final var e : edges) {
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
}

