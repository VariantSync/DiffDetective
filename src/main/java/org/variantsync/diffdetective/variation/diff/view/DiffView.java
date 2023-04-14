package org.variantsync.diffdetective.variation.diff.view;

import org.eclipse.jgit.diff.*;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.view.TreeView;
import org.variantsync.diffdetective.variation.tree.view.query.Query;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Consumer;
import java.util.regex.Pattern;

public class DiffView {
    public static final Pattern HUNK_HEADER_REGEX = Pattern.compile("@@\\s-(\\d+).*\\+(\\d+).*@@(\\r\\n|\\r|\\n)");

    private static void forMeAndMyAncestors(final DiffNode n, Time t, Consumer<DiffNode> callback) {
        callback.accept(n);
        final DiffNode p = n.getParent(t);
        if (p != null) {
            forMeAndMyAncestors(p, t, callback);
        }
    }

    private static DiffFormatter makeFormatterWithoutHeader(final OutputStream os) {
        return new DiffFormatter(os) {
            @Override
            protected void writeHunkHeader(int aStartLine, int aEndLine, int bStartLine, int bEndLine) {

            }
        };
    }

    public static DiffTree naive(final DiffTree d, final Query q) throws IOException, DiffParseException {
//        Logger.info("q = " + q);
        final String[] projectionViewText = new String[2];
        final RawText[] text = new RawText[2];

        for (final Time t : Time.values()) {
            final int i = t.ordinal();

            final VariationTree projection = d.project(t);
            try {
                TreeView.treeInline(projection, q);
            } catch (NullPointerException e) {
                Logger.info(q);
                throw e;
            }

            final StringBuilder b = new StringBuilder();
            projection.root().printSourceCode(b);
            projectionViewText[i] = b.toString();
            text[i] = new RawText(projectionViewText[i].getBytes());

//            Logger.info("t = " + t + "\n" + s);
        }

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

        final DiffTree view = DiffTreeParser.createDiffTree(textDiff,
                new DiffTreeParseOptions(
                        true,
                        true
                ));
        view.setSource(new ViewSource(d, q));
//        Logger.info("success");
        return view;
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
        DiffNode rootCopy = null;

        // Step 1: Determine R
        D.forAll(node -> Time.forAll(t -> {
            if (node.diffType.existsAtTime(t) && q.test(node.projection(t))) {
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
            boolean isRoot = true;
            for (Time t : timesOfRelevancy) {
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
                Assert.assertNull(rootCopy);
                rootCopy = copy;
            }
        }

        // Step 3: Embed edges in OOP.
        edges.sort(Comparator.comparingInt(Edge::index));
        for (final Edge edge : edges) {
            toCopy.get(edge.parentInD()).addChild(edge.childCopy(), edge.t());
        }

        // Step 4: Build return value
        Assert.assertNotNull(rootCopy);
        return new DiffTree(rootCopy, new ViewSource(D, q));
    }
}
