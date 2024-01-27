package org.variantsync.diffdetective.variation.diff.view;

import org.eclipse.jgit.diff.DiffAlgorithm;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.experiments.views.Main;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.CollectionUtils;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.*;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;
import org.variantsync.functjonal.Cast;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.function.BiPredicate;

/**
 * This class groups the implementations for functions that generate views on variation diffs,
 * as described in Chapter 5 of our SPLC'23 paper - Views on Edits to Variational Software.
 */
public class DiffView {
    /**
     * Translates a relevance predicate for nodes on variation trees to a relevance predicate on nodes in a
     * variation diff.
     * The returned predicate determines if a node in the given variation diff is relevant at a given time.
     * @param d The variation diff on which relevance of nodes should be determined.
     * @param rho A relevance predicate for view generation.
     * @return A binary predicate that determines whether a node in a variation diff is relevant at a given time.
     *         The variation diff node has to be given in terms of its projection at the corresponding time.
     */
    public static <L extends Label> BiPredicate<Time, Projection<L>> computeWhenNodesAreRelevant(final VariationDiff<L> d, final Relevance rho) {
        final Map<Time, Set<Projection<L>>> V = new HashMap<>();

        for (final Time t : Time.values()) {
            final Set<Projection<L>> relevantNodes_t = new HashSet<>();
            relevantNodes_t.add(d.getRoot().projection(t));
            rho.computeViewNodes(d.getRoot().projection(t), relevantNodes_t::add);
            V.put(t, relevantNodes_t);
        }

        return (t, p) -> V.get(t).contains(p);
    }
    
    /**
     * This method is not intended to be used directly and exists for optimization purposes only.
     * Instead, consider using {@link #naive(VariationDiff, Relevance)}.
     * <p>
     * This method behaves as {@link #naive(VariationDiff, Relevance)} but takes as additional parameter,
     * an array of length two that contains the text representation of the views on the two variation trees
     * of the given variation diff d.
     * This method assumes that
     * {@code projectionViewText[Time.BEFORE.ordinal()]} represents the text of {@code d.project(Time.BEFORE)}
     * and that
     * {@code projectionViewText[Time.AFTER.ordinal()]} represents the text of {@code d.project(Time.AFTER)}.
     * This array is used for a text-based differencing of the two texts.
     * The resulting diff is considered to be the view on the given variation diff d and is subsequently parsed
     * to a variation diff and returned.
     *
     * @param projectionViewText The text-based representation of the views on the two variation trees represented by
     *                           the given diff d.
     * @throws IOException When the text-based diffing fails because of an IO error.
     * @throws DiffParseException When the text-based diff could not be parsed to a variation diff.
     */
    private static <L extends Label> VariationDiff<DiffLinesLabel> naive(final VariationDiff<L> d, final Relevance rho, final String[] projectionViewText) throws IOException, DiffParseException {
        final VariationDiff<DiffLinesLabel> view;
        try {
            view = JGitDiff.diff(projectionViewText[0], projectionViewText[1], DiffAlgorithm.SupportedAlgorithm.MYERS, Main.VARIATION_DIFF_PARSE_OPTIONS);
        } catch (DiffParseException e) {
            Logger.error("""
                            Could not parse diff obtained with query {} at {}:
                            Diff:
                            """,
                    d.getSource(), rho);
            throw e;
        }
        view.setSource(new ViewSource<>(d, rho));

        return view;
    }
    
    /**
     * This method is not intended to be used directly and exists for optimization purposes only.
     * Instead, consider using {@link #naive(VariationDiff, Relevance)}.
     * This method is used to compare different the naive view generation to other algorithms, independently
     * of the shared preprocessing in terms of {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)}.
     * <p>
     * This method behaves as {@link #naive(VariationDiff, Relevance)} but takes as additional parameter,
     * the translation of the given relevance predicate to a relevance on a variation diff.
     * This additional parameter is assumed to be created from the given relevance predicate
     * in terms of {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)}.
     * 
     * @param inView {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)} for the given variation diff d
     *                                                                        and relevance predicate rho.
     * @throws IOException When the text-based diffing fails because of an IO error.
     * @throws DiffParseException When the text-based diff could not be parsed to a variation diff.
     */
    public static <L extends Label> VariationDiff<DiffLinesLabel> naive(final VariationDiff<L> d, final Relevance rho, final BiPredicate<Time, Projection<L>> inView) throws IOException, DiffParseException {
        final String[] projectionViewText = new String[2];

        for (final Time t : Time.values()) {
            final int i = t.ordinal();

            final Map<Projection<L>, VariationTreeNode<L>> copyMemory = new HashMap<>();
            final VariationTree<L> treeView = new VariationTree<>(
                    d.getRoot().projection(t).toVariationTree(copyMemory),
                    new ProjectionSource<>(d, t)
            );

            // TODO: Avoid inversion by building the map in the correct way in the first place.
            final Map<VariationTreeNode<L>, Projection<L>> invCopyMemory = CollectionUtils.invert(copyMemory, HashMap::new);
            TreeView.treeInline(treeView.root(), v -> inView.test(t, invCopyMemory.get(v)));

            final StringBuilder b = new StringBuilder();
            treeView.root().printSourceCode(b);
            projectionViewText[i] = b.toString();
        }

        return naive(d, rho, projectionViewText);
    }

    /**
     * This function generates a view on the given variation diff by generating views on the underlying
     * variation trees, and then differencing these tree views.
     * Implementation of the function view_naive (Equation 8) from SPLC'23 paper - Views on Edits to Variational Software.
     * @param d The variation diff to generate a view on.
     * @param rho A relevance predicate that determines which nodes should be contained in the view.
     * @return A variation diff that constitutes a view on the given variation diff.
     * @throws IOException When the text-based diffing fails because of an IO error.
     * @throws DiffParseException When the text-based diff could not be parsed to a variation diff.
     */
    public static <L extends Label> VariationDiff<DiffLinesLabel> naive(final VariationDiff<L> d, final Relevance rho) throws IOException, DiffParseException {
        return naive(d, rho, DiffView.computeWhenNodesAreRelevant(d, rho));
    }

    /**
     * An alternative algorithm for generating of views on variation diffs based on 
     * (1) removing cycles in the variation diff,
     * (2) interpreting the resulting acyclic variation diff as a colored variation tree,
     * (3) creating a view on the variation tree,
     * (4) and finally reintroducing the removed cycles.
     * @param d The variation diff to generate a view on.
     * @param rho A relevance predicate that determines which nodes should be contained in the view.
     * @return A variation diff that constitutes a view on the given variation diff.
     */
    public static <L extends Label> VariationDiff<L> badgood(final VariationDiff<L> d, final Relevance rho) {
        // treeify
        final BadVDiff<L> badDiff = BadVDiff.fromGood(d);

        // create view
        TreeView.treeInline(badDiff.diff(), rho);

        // unify
        final VariationDiff<L> goodDiff = badDiff.toGood();
        goodDiff.assertConsistency();
        return goodDiff;
    }


    /**
     * This method is not intended to be used directly and exists for optimization purposes only.
     * Instead, consider using {@link #optimized(VariationDiff, Relevance)}.
     * This method is used to compare different the naive view generation to other algorithms, independently
     * of the shared preprocessing in terms of {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)}.
     * <p>
     * This method behaves as {@link #optimized(VariationDiff, Relevance)} but takes as additional parameter,
     * the translation of the given relevance predicate to a relevance on a variation diff.
     * This additional parameter is assumed to be created from the given relevance predicate
     * in terms of {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)}.
     *
     * @param inView {@link #computeWhenNodesAreRelevant(VariationDiff, Relevance)} for the given variation diff d
     *                                                                        and relevance predicate rho.
     */
    public static <L extends Label> VariationDiff<L> optimized(final VariationDiff<L> d, final Relevance rho, final BiPredicate<Time, Projection<L>> inView) {
        /*
         * Memorization of translated nodes.
         * Keys are the nodes in rho.
         * Values are copies of keys to return.
         */
        final Map<DiffNode<L>, DiffNode<L>> toCopy = new HashMap<>();

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
        record Edge<L extends Label>(DiffNode<L> childCopy, DiffNode<L> parentInD, Time t, int index) {}
        final List<Edge<L>> edges = new ArrayList<>();

        // Memoization of the copy of the root.
        final DiffNode<L>[] rootCopy = Cast.unchecked(Array.newInstance(DiffNode.class, 1));

        // Create copy nodes and edges.
        // We also find the root here.
        d.forAll(node -> {
            final DiffType nodeDiffType = node.getDiffType();
            final Set<Time> timesOfRelevancy = new HashSet<>();
            for (final Time t : Time.values()) {
                if (nodeDiffType.existsAtTime(t) && inView.test(t, node.projection(t))) {
                    timesOfRelevancy.add(t);
                }
            }

            final DiffType dt;
            {
                final Optional<DiffType> odt = DiffType.thatExistsOnlyAtAll(timesOfRelevancy);
                if (odt.isEmpty()) {
                    return;
                } else {
                    dt = odt.get();
                }
            }

            // create copy
            final DiffNode<L> copy = new DiffNode<>(
                    dt,
                    node.getNodeType(),
                    node.getFromLine(),
                    node.getToLine(),
                    node.getFormula(),
                    node.getLabel()
            );
            toCopy.put(node, copy);

            // connect to parent + find root
            boolean isRoot = true;
            for (final Time t : timesOfRelevancy) {
                final DiffNode<L> parent = node.getParent(t);
                if (parent != null) {
                    edges.add(new Edge<L>(
                            copy,
                            parent,
                            t,
                            parent.indexOfChild(node, t)
                    ));
                    isRoot = false;
                }
            }

            if (isRoot) {
                Assert.assertNull(rootCopy[0]);
                rootCopy[0] = copy;
            }
        });

        // Step 3: Embed edges in OOP.
        edges.sort(Comparator.comparingInt(Edge::index));
        for (final Edge<L> edge : edges) {
            final DiffNode<L> parentInView = toCopy.get(edge.parentInD());
            if (parentInView == null) {
                Assert.assertTrue(parentInView != null, () -> "Node " + edge.childCopy + " has no parent in view given by " + rho + " in " + d.getSource());
            }
            parentInView.addChild(edge.childCopy(), edge.t());
        }

        // Step 4: Build return value
        Assert.assertNotNull(rootCopy[0]);
        return new VariationDiff<>(rootCopy[0], new ViewSource<>(d, rho));
    }

    /**
     * This function generates a view on the given variation diff by determining the times of relevance for each node.
     * Implementation of the function view_smart (Equation 10) from SPLC'23 paper - Views on Edits to Variational Software.
     * @param d The variation diff to generate a view on.
     * @param rho A relevance predicate that determines which nodes should be contained in the view.
     * @return A variation diff that constitutes a view on the given variation diff.
     */
    public static <L extends Label> VariationDiff<L> optimized(final VariationDiff<L> d, final Relevance rho) {
        return optimized(d, rho, computeWhenNodesAreRelevant(d, rho));
    }
}
