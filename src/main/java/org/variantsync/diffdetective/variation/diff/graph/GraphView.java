package org.variantsync.diffdetective.variation.diff.graph;

import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;

import java.util.*;

public record GraphView(
        Set<DiffNode> nodes,
        Set<Edge> edges
) {
    public record Edge (DiffNode child, DiffNode parent, Time time) {
    }

    public static GraphView fromDiffTree(final DiffTree d) {
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

        return new GraphView(nodes, edges);
    }

    @Override
    public String toString() {
        final StringBuilder b = new StringBuilder();

        for (final DiffNode v : nodes) {
            b.append(v.getID()).append(": ").append(v).append(StringUtils.LINEBREAK);
        }

        for (final Edge e : edges) {
            b.append(e.child.getID()).append(" --").append(e.time).append("--> ").append(e.parent.getID()).append(StringUtils.LINEBREAK);
        }

        return b.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        final GraphView graph = (GraphView) o;

        return     nodes.equals(graph.nodes)
                && edges.equals(graph.edges);
    }

    @Override
    public int hashCode() {
        return Objects.hash(nodes, edges);
    }
}

