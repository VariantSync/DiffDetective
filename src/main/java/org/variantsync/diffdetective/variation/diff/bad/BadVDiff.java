package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.diff.text.DiffLineNumberRange;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.map.MapUtils;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

import static org.variantsync.diffdetective.variation.diff.DiffType.NON;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * A bad variation diff is a variation diff that has no subtree sharing.
 * This means that the diff is a tree.
 * We thus can store the diff as a variation tree with some extra information.
 * This allows us to prove the variation diff being a tree at the type level.
 * <p>
 * When converting a variation diff to a bad one, cycles
 * <pre>
 *    .
 *   / \
 *  /   \
 * .     .
 * \    /
 *  \  /
 *   x
 * </pre>
 * are resolved by cloning the deepest subtree x in the cycle.
 * <pre>
 *    .
 *   / \
 *  /   \
 * .     .
 * |     |
 * |     |
 * x     x
 * </pre>
 * The matching in a bad variation diff stores which nodes have been cloned.
 * <p>
 * As variation trees do not store any diff-specific information, we remember the
 * nodes difftype in the original variation diff in a coloring map.
 * <p>
 * (Unproven) Invariants:
 * for all VariationDiff d: fromGood(d).toGood() equals d
 *
 * @param diff The variation tree that models the tree diff.
 * @param matching Memorization of which nodes are clones and can be safely merged when converting back
 *                 to a variation diff.
 * @param coloring Memorization of the diff types of all nodes.
 * @param lines Memorization of line ranges within text diffs.
 * @author Paul Bittner
 */
public record BadVDiff<L extends Label>(
        VariationTree<L> diff,
        Map<VariationTreeNode<L>, VariationTreeNode<L>> matching,
        Map<VariationTreeNode<L>, DiffType> coloring,
        Map<VariationTreeNode<L>, DiffLineNumberRange> lines
)
{
    /**
     * Memoization of the VariationTreeNodes a DiffNode was
     * converted to in {@link #fromGood(VariationDiff)}.
     */
    private static class FromGoodNodeTranslation<L extends Label> {
        private final Map<DiffNode<L>, Map<Time, VariationTreeNode<L>>> translate = new HashMap<>();

        /**
         * Remember that the given DiffNode d was translated to the given
         * VariationTreeNode v at all times d exists.
         * @param d The DiffNode from the initial good VariationDiff.
         * @param v The VariationTreeNode d was translated to.
         */
        void put(DiffNode<L> d, VariationTreeNode<L> v) {
            d.getDiffType().forAllTimesOfExistence(
                    t -> put(d, t, v)
            );
        }

        /**
         * Remember that the given DiffNode d was translated to the given
         * VariationTreeNode v at time t.
         * @param d The DiffNode from the initial good VariationDiff.
         * @param t The time at which the translated node v represents the initial DiffNode d.
         * @param v The VariationTreeNode d was translated to.
         */
        void put(DiffNode<L> d, Time t, VariationTreeNode<L> v) {
            translate.putIfAbsent(d, new HashMap<>());
            translate.get(d).put(t, v);
        }

        /**
         * Returns the VariationTreeNode that represents the given DiffNode d
         * at the given time t in the produced bad diff.
         *
         * @param d The DiffNode from the initial good VariationDiff.
         * @param t The time for which we seek the node that represents the initial DiffNode d.
         */
        VariationTreeNode<L> get(DiffNode<L> d, Time t) {
            return translate.getOrDefault(d, new HashMap<>()).get(t);
        }
    }

    /**
     * Plain conversion of DiffNodes to VariationTree nodes.
     * Copies {@link org.variantsync.diffdetective.variation.NodeType node type},
     *        {@link DiffNode#getFormula() formula},
     *        {@link DiffNode#getLinesInDiff() line numbers in the diff},
     *        and {@link DiffNode#getLabelLines() label} but no edge information.
     * @param n The node to convert to a plain VariationTreeNode.
     */
    private static <L extends Label> VariationTreeNode<L> plain(final DiffNode<L> n) {
        return new VariationTreeNode<>(
                n.getNodeType(),
                n.getFormula(),
                n.getLinesInDiff(),
                n.getLabel()
        );
    }

    /**
     * Performs a {@link #plain(DiffNode) plain} conversion of the given DiffNode n
     * to a VariationTreeNode.
     * Additionally, stores metadata to invert this operation in the given maps.
     * For further information on these maps,
     * have a look at {@link BadVDiff the documentation of this class}.
     *
     * @param nodeTranslation see {@link FromGoodNodeTranslation}
     */
    private static <L extends Label> VariationTreeNode<L> fromGood(
            final DiffNode<L> n,
            final FromGoodNodeTranslation<L> nodeTranslation,
            final Map<VariationTreeNode<L>, DiffType> coloring,
            final Map<VariationTreeNode<L>, DiffLineNumberRange> lines) {
        final VariationTreeNode<L> result = plain(n);
        nodeTranslation.put(n, result);
        coloring.put(result, n.getDiffType());
        lines.put(result, new DiffLineNumberRange(n.getFromLine(), n.getToLine()));
        return result;
    }

    /**
     * Inverse of {@link #fromGood(DiffNode, FromGoodNodeTranslation, Map, Map)}.
     */
    private DiffNode<L> toGood(final VariationTreeNode<L> n) {
        final DiffLineNumberRange nlines = lines.get(n);
        return new DiffNode<L>(
                coloring.get(n),
                n.getNodeType(),
                nlines.from(),
                nlines.to(),
                n.getFormula(),
                n.getLabel()
        );
    }

    /**
     * Merges two VariationTreeNodes that represent the same node to a single unchanged DiffNode.
     * It is asserted that both given nodes have the same {@link org.variantsync.diffdetective.variation.NodeType},
     * label, formula, and line numbers.
     */
    private DiffNode<L> mergeToGood(final VariationTreeNode<L> before, final VariationTreeNode<L> after) {
        Assert.assertEquals(before.getNodeType(), after.getNodeType());
        Assert.assertEquals(lines.get(before), lines.get(after));
        Assert.assertEquals(before.getFormula(), after.getFormula());
        Assert.assertEquals(before.getLabel(), after.getLabel());

        final DiffLineNumberRange nlines = lines.get(before);

        return new DiffNode<>(
                NON,
                before.getNodeType(),
                nlines.from(),
                nlines.to(),
                before.getFormula(),
                before.getLabel()
        );
    }

    /**
     * Creates a bad diff from a VariationDiff.
     * @param d The VariationDiff to convert to a bad diff.
     * @see BadVDiff
     */
    public static <L extends Label> BadVDiff<L> fromGood(VariationDiff<L> d) {
        record EdgeToConstruct<L extends Label>(
                VariationTreeNode<L> child,
                DiffNode<L> parent,
                Time t
        ) {}

        final FromGoodNodeTranslation<L> nodeTranslation = new FromGoodNodeTranslation<>();

        final Map<VariationTreeNode<L>, VariationTreeNode<L>> matching = new HashMap<>();
        final Map<VariationTreeNode<L>, DiffType>             coloring = new HashMap<>();
        final Map<VariationTreeNode<L>, DiffLineNumberRange>  lines    = new HashMap<>();
        final Set<DiffNode<L>> splittedNodes = new HashSet<>();

        final List<EdgeToConstruct<L>> edgesToConstruct = new ArrayList<>();

        final VariationTreeNode<L> root = fromGood(d.getRoot(), nodeTranslation, coloring, lines);

        d.forAll(diffNode -> {
            if (diffNode == d.getRoot()) {
                return;
            }

            final DiffNode<L> pbefore = diffNode.getParent(BEFORE);
            final DiffNode<L> pafter = diffNode.getParent(AFTER);

            final DiffType color = diffNode.getDiffType();

            final boolean unchanged = color == NON;

            /*
             * The parent of a node was not split if this node has
             * a single parent and that parent is unchanged.
             */
            final boolean hasUnsplitUnchangedParent =
                       pbefore == pafter
                    && !splittedNodes.contains(pbefore);
            if (hasUnsplitUnchangedParent) {
                // Assert that the variation diff is not ill-formed.
                Assert.assertTrue(pbefore != null);
                Assert.assertTrue(pbefore.isNon());
            }

            /*
             * We split every node that is unchanged and whose parent was also splitted.
             * In fact, we only have to split unchanged nodes but the second clause makes the bad diff
             * a bit less bad by splitting unchanged nodes only when necessary.
             * Basically, a variation tree can represent unchanged nodes when there were no changes
             * above the unchanged node.
             * By removing the second clause, we obtain a bad diff that splits in exactly the two
             * projections of the variation diff.
             */
            final boolean split = unchanged && !hasUnsplitUnchangedParent;
            if (split) {
                Assert.assertTrue(pbefore != null && pafter != null);

                final DiffLineNumberRange dRange = new DiffLineNumberRange(diffNode.getFromLine(), diffNode.getToLine());

                VariationTreeNode<L>[] selfs = Cast.unchecked(Array.newInstance(VariationTreeNode.class, 2));
                Time.forAll(time -> {
                    final VariationTreeNode<L> self = plain(diffNode);
                    selfs[time.ordinal()] = self;

                    nodeTranslation.put(diffNode, time, self);

                    edgesToConstruct.add(new EdgeToConstruct<>(self, diffNode.getParent(time), time));

                    // further metadata to copy
                    lines.put(self, dRange);
                    coloring.put(self, DiffType.thatExistsOnlyAt(time));
                });

                matching.put(selfs[0], selfs[1]);
                matching.put(selfs[1], selfs[0]);

                splittedNodes.add(diffNode);
            } else {
                final VariationTreeNode<L> self = fromGood(diffNode, nodeTranslation, coloring, lines);

                /*
                 * Else is important in the following branching:
                 * - It does not affect added or removed nodes as these will have only one parent anyway.
                 * - For unchanged nodes, we want to construct only one parent edge in the resulting
                 *   variation tree. In a variation tree, every node has only one parent and when we would
                 *   issue two parallel edges to be constructed here, then we variation tree construction would
                 *   fail since every node can have only one parent.
                 */
                if (pbefore != null) {
                    edgesToConstruct.add(new EdgeToConstruct<>(
                            self, pbefore, BEFORE
                    ));
                } else if (pafter != null) {
                    edgesToConstruct.add(new EdgeToConstruct<>(
                            self, pafter, AFTER
                    ));
                }
            }
        });

        for (final EdgeToConstruct<L> e : edgesToConstruct) {
            nodeTranslation.get(e.parent, e.t).addChild(e.child);
        }

        return new BadVDiff<>(
                new VariationTree<>(root, new BadVDiffFromVariationDiffSource(d.getSource())),
                matching,
                coloring,
                lines
        );
    }

    /**
     * Inverse of {@link #fromGood(VariationDiff)}.
     * Restores the VariationDiff that is represented by this bad tree diff.
     */
    public VariationDiff<L> toGood() {
        /*
        Store the command to construct an edge from the given child to the
        given parent at the given time.

        We cannot construct edges in place and have to use this indirect command.
        Constructing edges requires both nodes of the edge to already have
        been translated.
        When finding a node x that has to be merged, the parent p of the merge target y
        (i.e., another node in the tree with which x has to be merged) has not yet been translated.
        Thus, we cannot construct an edge from the translation of x to the translation of p in place.
         */
        record EdgeToConstruct<L extends Label>(
                DiffNode<L> child,
                VariationTreeNode<L> parent,
                Time time
        ) {}

        final List<EdgeToConstruct<L>>               edgesToConstruct = new ArrayList<>();
        final Map<VariationTreeNode<L>, DiffNode<L>> nodeTranslation  = new HashMap<>();

        final DiffNode<L> root = toGood(diff.root());
        nodeTranslation.put(diff.root(), root);

        diff.forAllPreorder(vtnode -> {
            // If a node was already translated (because it was merged), it does not have to be translated anymore.
            // We already translated the root, so we can skip it (which we do since it is already a key on nodeTranslation).
            if (nodeTranslation.containsKey(vtnode)) {
                return;
            }

            final VariationTreeNode<L> parent = vtnode.getParent();
            Assert.assertNotNull(parent);

            final VariationTreeNode<L> badBuddy = matching.get(vtnode);
            if (badBuddy == null || !diff.contains(badBuddy)) {
                // v was not cloned.
                // We can just directly convert it to a DiffNode.
                final DiffNode<L> vGood = toGood(vtnode);

                nodeTranslation.put(vtnode, vGood);
                coloring.get(vtnode).forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct<>(vGood, parent, t))
                );
            } else {
                // v was cloned.
                // We have to merge it with its cloning partner.
                final DiffNode<L> vGood = mergeToGood(vtnode, badBuddy);

                final DiffType vColor = coloring.get(vtnode);
                final DiffType badBuddyColor = coloring.get(badBuddy);
                Assert.assertTrue(vColor.inverse() == badBuddyColor);

                nodeTranslation.put(vtnode, vGood);
                nodeTranslation.put(badBuddy, vGood);

                // Since the colors are ADD and REM, both following calls will only
                // invoke the callback for a single time:
                // BEFORE for REM and AFTER for ADD.
                vColor.forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct<>(vGood, parent, t))
                );
                badBuddyColor.forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct<>(vGood, badBuddy.getParent(), t))
                );
            }
        });

        for (final EdgeToConstruct<L> e : edgesToConstruct) {
            nodeTranslation.get(e.parent()).addChild(e.child(), e.time());
        }

        VariationDiffSource source = VariationDiffSource.Unknown;
        if (diff.source() instanceof BadVDiffFromVariationDiffSource s) {
            source = s.initialVariationDiff();
        }

        return new VariationDiff<>(root, source);
    }

    public BadVDiff<L> deepCopy() {
        final Map<VariationTreeNode<L>, VariationTreeNode<L>> oldToNew = new HashMap<>();
        final VariationTree<L> diffCopy = diff().deepCopy(oldToNew);

        final Map<VariationTreeNode<L>, VariationTreeNode<L>> matchingCopy = new HashMap<>();
        for (Map.Entry<VariationTreeNode<L>, VariationTreeNode<L>> entry : matching().entrySet()) {
            matchingCopy.put(
                    oldToNew.get(entry.getKey()),
                    oldToNew.get(entry.getValue())
            );
        }

        return new BadVDiff<>(
                diffCopy,
                matchingCopy,
                MapUtils.TransKeys(coloring(), oldToNew, HashMap::new),
                MapUtils.TransKeys(lines(),    oldToNew, HashMap::new)
        );
    }

    private void prettyPrint(final String indent, StringBuilder b, VariationTreeNode<L> n) {
        if (!n.isRoot()) {
            final String prefix = coloring.get(n).symbol + indent;
            b.append(n.getLabel().getLines().stream().collect(Collectors.joining(
                    StringUtils.LINEBREAK + prefix,
                            prefix,
                            StringUtils.LINEBREAK)
            ));
        }

        for (VariationTreeNode<L> child : n.getChildren()) {
            prettyPrint("  " + indent, b, child);
        }

        if (n.getNodeType().isAnnotation() && !n.isRoot()) {
            b
                    .append(coloring.get(n).symbol)
                    .append(indent)
                    .append("#endif")
                    .append(StringUtils.LINEBREAK);
        }
    }

    public String prettyPrint() {
        final StringBuilder b = new StringBuilder();
        prettyPrint("", b, diff.root());
        return b.toString();
    }
}
