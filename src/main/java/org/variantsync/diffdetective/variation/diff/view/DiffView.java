package org.variantsync.diffdetective.variation.diff.view;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class DiffView {
    private static void forMeAndMyAncestors(final DiffNode n, Time t, Consumer<DiffNode> callback) {
        callback.accept(n);
        final DiffNode p = n.getParent(t);
        if (p != null) {
            forMeAndMyAncestors(p, t, callback);
        }
    }

    public static DiffTree badgood(final DiffTree d, final Query q) {
        // treeify
        final BadVDiff badDiff = BadVDiff.fromGood(d);

        // create view
        TreeView.treeInline(badDiff.diff(), q);

        // unify
        final DiffTree goodDiff = badDiff.toGood();
        goodDiff.assertConsistency();
        return goodDiff;
    }

    public static DiffTree optimized(final DiffTree D, final Query q) {
        /*
         * Set of relevant nodes R from the DiffTree D as for variation trees.
         * For variation diffs though, we also need to know at which times a node is relevant.
         */
        final Map<DiffNode, Set<Time>> R = new HashMap<>();

        /*
         * Memorization of translated nodes.
         * Keys are the nodes in R.
         * Values are copies of keys to return.
         */
        final Map<DiffNode, DiffNode> toCopy = new HashMap<>();

        /*
         * We have to separate edge from node construction because we can draw edges only if all nodes
         * have been translated.
         * So we first translate all nodes, then all edges.
         * An edge connects a child node (which is already a copy)
         * to a parent node (which is a node from D)
         * at time t.
         * The index is the index the child had below its parent at time t in D.
         * We use it to retain child ordering.
         */
        record Edge(DiffNode childCopy, DiffNode parentInD, Time t, int index) {}
        final List<Edge> edges = new ArrayList<>();

        // Memoization of the copy of the root.
        final DiffNode[] rootCopy = {null};

        // Step 1: Determine R
        D.forAll(node -> Time.forAll(t -> {
            if (node.diffType.existsAtTime(t) && q.isInteresting(node.projection(t))) {
                forMeAndMyAncestors(node, t, a -> R
                        .computeIfAbsent(a, _ignored -> new HashSet<>())
                        .add(t));
            }
        }));

        // Step 2: Create copy nodes and edges.
        //         We also find the root here.
        for (final Map.Entry<DiffNode, Set<Time>> relevantNodeAtTimes : R.entrySet()) {
            final DiffNode node = relevantNodeAtTimes.getKey();
            final Set<Time> timesOfRelevancy = relevantNodeAtTimes.getValue();

            /*
             * A DiffType exists because timesOfRelevancy is not empty
             * because our node is relevant and thus there must be at least
             * one time at which it is relevant.
             */
            final DiffType dt = DiffType.thatExistsOnlyAtAll(timesOfRelevancy).orElseThrow(
                    AssertionError::new
            );

            // create copy
            final DiffNode copy = new DiffNode(
                    dt,
                    node.getNodeType(),
                    node.getFromLine(),
                    node.getToLine(),
                    node.getFormula(),
                    node.getLabelLines()
            );
            toCopy.put(node, copy);

            // connect to parent + find root
            final AtomicBoolean isRoot = new AtomicBoolean(true);
            timesOfRelevancy.forEach(t -> {
                final DiffNode parent = node.getParent(t);
                if (parent != null) {
                    edges.add(new Edge(
                            copy,
                            parent,
                            t,
                            parent.indexOfChild(node)
                    ));
                    isRoot.set(false);
                }
            });

            if (isRoot.get()) {
                Assert.assertNull(rootCopy[0]);
                rootCopy[0] = copy;
            }
        }

        // Step 3: Embed edges in OOP.
        edges.sort(Comparator.comparingInt(Edge::index));
        for (final Edge edge : edges) {
            toCopy.get(edge.parentInD()).addChild(edge.childCopy(), edge.t());
        }

        // Step 4: Build return value
        Assert.assertNotNull(rootCopy[0]);
        return new DiffTree(rootCopy[0], DiffTreeSource.Unknown);
    }
}
