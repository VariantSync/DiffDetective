package org.variantsync.diffdetective.variation.diff.view;

import org.eclipse.jgit.diff.*;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.experiments.views.Main;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.CollectionUtils;
import org.variantsync.diffdetective.variation.diff.*;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.relevance.Relevance;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.BiPredicate;
import java.util.regex.Pattern;

public class DiffView {
    public static final Pattern HUNK_HEADER_REGEX = Pattern.compile("@@\\s-(\\d+).*\\+(\\d+).*@@(\\rho\\n|\\rho|\\n)");

    private static DiffFormatter makeFormatterWithoutHeader(final OutputStream os) {
        return new DiffFormatter(os) {
            @Override
            protected void writeHunkHeader(int aStartLine, int aEndLine, int bStartLine, int bEndLine) {

            }
        };
    }

    public static BiPredicate<Time, Projection> computeWhenNodesAreRelevant(final DiffTree d, final Relevance rho) {
        final Map<Time, Set<Projection>> V = new HashMap<>();

        for (final Time t : Time.values()) {
            final Set<Projection> relevantNodes_t = new HashSet<>();
            relevantNodes_t.add(d.getRoot().projection(t));
            rho.computeViewNodes(d.getRoot().projection(t), relevantNodes_t::add);
            V.put(t, relevantNodes_t);
        }

        return (t, p) -> V.get(t).contains(p);
    }

    public static DiffTree naive(final DiffTree d, final Relevance rho, final String[] projectionViewText) throws IOException, DiffParseException {
//        Logger.info("q = " + q);
        final RawText[] text = new RawText[] {
                new RawText(projectionViewText[Time.BEFORE.ordinal()].getBytes()),
                new RawText(projectionViewText[Time.AFTER.ordinal()].getBytes())
        };

        // MYERS or HISTOGRAM
        final DiffAlgorithm diffAlgorithm = DiffAlgorithm.getAlgorithm(DiffAlgorithm.SupportedAlgorithm.MYERS);
        final RawTextComparator comparator = RawTextComparator.DEFAULT;
        final EditList diff = diffAlgorithm.diff(
                comparator,
                text[Time.BEFORE.ordinal()],
                text[Time.AFTER.ordinal()]
        );

        String textDiff;
        {
            final ByteArrayOutputStream os = new ByteArrayOutputStream();

            /*
            Using our own formatter without diff headers (paired with a maximum context (?))
            caused the formatter to crash due to index out of bounds exceptions.
            So I guess there is a hidden assumption in the DiffFormatter that expects the header
            to be there.

            As a fix, we also use our own construction of embedding patches into the before file to obtain a full diff.
             */
            // final DiffFormatter formatter = makeFormatterWithoutHeader(os);
            // formatter.setContext(Integer.MAX_VALUE); // FULL DIFF
            final DiffFormatter formatter = new DiffFormatter(os);

            formatter.setDiffAlgorithm(diffAlgorithm);
            formatter.setDiffComparator(comparator);
            formatter.setOldPrefix("");
            formatter.setNewPrefix("");

            formatter.format(
                    diff,
                    text[Time.BEFORE.ordinal()],
                    text[Time.AFTER.ordinal()]);
            formatter.flush();
            textDiff = os.toString(StandardCharsets.UTF_8);
            formatter.close();
            os.close();

//            Logger.info("Initial Diff\n" + textDiff);

            textDiff = GitDiffer.getFullDiff(
                    new BufferedReader(new StringReader(projectionViewText[Time.BEFORE.ordinal()])),
                    new BufferedReader(new StringReader(textDiff))
            );

            //textDiff = textDiff.replace("\\ No newline at end of file\n", "");
            //textDiff = HUNK_HEADER_REGEX.matcher(textDiff).replaceAll("");
        }
//        Logger.info("Full Diff\n" + textDiff);
        final DiffTree view;
        try {
            view = DiffTreeParser.createDiffTree(textDiff, Main.DIFFTREE_PARSE_OPTIONS);
        } catch (DiffParseException e) {
            Logger.error("""
                            Could not parse diff obtained with query {} at {}:
                            Diff:
                            """,
                    d.getSource(), rho);
            System.out.println(textDiff);
            throw e;
        }
        view.setSource(new ViewSource(d, rho));

        return view;
    }

    public static DiffTree naive(final DiffTree d, final Relevance rho, final BiPredicate<Time, Projection> inView) throws IOException, DiffParseException {
        final String[] projectionViewText = new String[2];

        for (final Time t : Time.values()) {
            final int i = t.ordinal();

            final Map<Projection, VariationTreeNode> copyMemory = new HashMap<>();
            final VariationTree treeView = new VariationTree(
                    d.getRoot().projection(t).toVariationTree(copyMemory),
                    new ProjectionSource(d, t)
            );

            // TODO: Avoid inversion by building the map in the correct way in the first place.
            final Map<VariationTreeNode, Projection> invCopyMemory = CollectionUtils.invert(copyMemory, HashMap::new);
            TreeView.treeInline(treeView.root(), v -> inView.test(t, invCopyMemory.get(v)));

            final StringBuilder b = new StringBuilder();
            treeView.root().printSourceCode(b);
            projectionViewText[i] = b.toString();
        }

        return naive(d, rho, projectionViewText);
    }

    @Deprecated
    public static DiffTree naive(final DiffTree d, final Relevance rho) throws IOException, DiffParseException {
        final String[] projectionViewText = new String[2];

        for (final Time t : Time.values()) {
            final int i = t.ordinal();

            final VariationTree projection = d.project(t);
            try {
                TreeView.treeInline(projection, rho);
            } catch (NullPointerException e) {
                Logger.info(rho);
                throw e;
            }

            final StringBuilder b = new StringBuilder();
            projection.root().printSourceCode(b);
            projectionViewText[i] = b.toString();
        }
        return naive(d, rho, projectionViewText);
    }

    public static DiffTree badgood(final DiffTree d, final Relevance q) {
        // treeify
        final BadVDiff badDiff = BadVDiff.fromGood(d);

        // create view
        TreeView.treeInline(badDiff.diff(), q);

        // unify
        final DiffTree goodDiff = badDiff.toGood();
        goodDiff.assertConsistency();
        return goodDiff;
    }

    public static DiffTree optimized(final DiffTree D, final Relevance rho, final BiPredicate<Time, Projection> inView) {
        /*
         * Memorization of translated nodes.
         * Keys are the nodes in rho.
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

        // Create copy nodes and edges.
        // We also find the root here.
        D.forAll(node -> {
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
            boolean isRoot = true;
            for (final Time t : timesOfRelevancy) {
                final DiffNode parent = node.getParent(t);
                if (parent != null) {
                    edges.add(new Edge(
                            copy,
                            parent,
                            t,
                            parent.indexOfChild(node)
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
        for (final Edge edge : edges) {
            final DiffNode parentInView = toCopy.get(edge.parentInD());
            if (parentInView == null) {
                Assert.assertTrue(parentInView != null, () -> "Node " + edge.childCopy + " has no parent in view given by " + rho + " in " + D.getSource());
            }
            parentInView.addChild(edge.childCopy(), edge.t());
        }

        // Step 4: Build return value
        Assert.assertNotNull(rootCopy[0]);
        return new DiffTree(rootCopy[0], new ViewSource(D, rho));
    }

    public static DiffTree optimized(final DiffTree D, final Relevance q) {
        return optimized(D, q, computeWhenNodesAreRelevant(D, q));
    }
}
