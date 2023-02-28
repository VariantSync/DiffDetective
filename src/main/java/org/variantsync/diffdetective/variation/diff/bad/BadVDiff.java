package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;

import java.util.*;
import java.util.stream.Collectors;

import static org.variantsync.diffdetective.variation.diff.DiffType.*;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * A bad variation diff is a variation diff that has no subtree sharing.
 * This means that the diff is a tree.
 * We thus can store the diff as a variation tree with some extra information.
 * This allows us to prove the variation diff being a tree at the type level.
 * <p>
 * When converting a variation diff to a bad one, cycles
 *    .
 *   / \
 *  /   \
 * .     .
 * \    /
 *  \  /
 *   x
 * are resolved by cloning the deepest subtree x in the cycle.
 *    .
 *   / \
 *  /   \
 * .     .
 * |     |
 * |     |
 * x     x
 * The matching in a bad variation diff stores which nodes have been cloned.
 * <p>
 * As variation trees do not store any diff-specific information, we remember the
 * nodes difftype in the original variation diff in a coloring map.
 * <p>
 * (Unproven) Invariants:
 * for all DiffTree d: fromGood(d).toGood() equals d
 *
 * @param diff The variation tree that models the tree diff.
 * @param matching Memorization of which nodes are clones and can be safely merged when converting back
 *                 to a variation diff.
 * @param coloring Memorization of the diff types of all nodes.
 * @author Paul Bittner
 */
public record BadVDiff
        (VariationTree diff,
         Map<VariationTreeNode, VariationTreeNode> matching,
         Map<VariationTreeNode, DiffType> coloring,
         Map<VariationTreeNode, DiffLineNumberRange> lines
         )
{
    /**
     * Memoization of the VariationTreeNodes a DiffNode was
     * converted to in {@link #fromGood(DiffTree)}.
     */
    private static class FromGoodNodeTranslation {
        private final Map<DiffNode, Map<Time, VariationTreeNode>> translate = new HashMap<>();

        /**
         * Remember that the given DiffNode d was translated to the given
         * VariationTreeNode v at all times d exists.
         * @param d The DiffNode from the initial good DiffTree.
         * @param v The VariationTreeNode d was translated to.
         */
        void put(DiffNode d, VariationTreeNode v) {
            d.getDiffType().forAllTimesOfExistence(
                    t -> put(d, t, v)
            );
        }

        /**
         * Remember that the given DiffNode d was translated to the given
         * VariationTreeNode v at time t.
         * @param d The DiffNode from the initial good DiffTree.
         * @param t The time at which the translated node v represents the initial DiffNode d.
         * @param v The VariationTreeNode d was translated to.
         */
        void put(DiffNode d, Time t, VariationTreeNode v) {
            translate.putIfAbsent(d, new HashMap<>());
            translate.get(d).put(t, v);
        }

        /**
         * Returns the VariationTreeNode that represents the given DiffNode d
         * at the given time t in the produced bad diff.
         *
         * @param d The DiffNode from the initial good DiffTree.
         * @param t The time for which we seek the node that represents the initial DiffNode d.
         */
        VariationTreeNode get(DiffNode d, Time t) {
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
    private static VariationTreeNode plain(final DiffNode n) {
        return new VariationTreeNode(
                n.getNodeType(),
                n.getFormula(),
                n.getLinesInDiff(),
                n.getLabelLines()
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
    private static VariationTreeNode fromGood(
            final DiffNode n,
            final FromGoodNodeTranslation nodeTranslation,
            final Map<VariationTreeNode, DiffType> coloring,
            final Map<VariationTreeNode, DiffLineNumberRange> lines) {
        final VariationTreeNode result = plain(n);
        nodeTranslation.put(n, result);
        coloring.put(result, n.getDiffType());
        lines.put(result, new DiffLineNumberRange(n.getFromLine(), n.getToLine()));
        return result;
    }

    /**
     * Inverse of {@link #fromGood(DiffNode, FromGoodNodeTranslation, Map, Map)}.
     */
    private DiffNode toGood(final VariationTreeNode n) {
        final DiffLineNumberRange nlines = lines.get(n);
        return new DiffNode(
                coloring.get(n),
                n.getNodeType(),
                nlines.from(),
                nlines.to(),
                n.getFormula(),
                n.getLabelLines()
        );
    }

    /**
     * Merges two VariationTreeNodes that represent the same node to a single unchanged DiffNode.
     * It is asserted that both given nodes have the same {@link org.variantsync.diffdetective.variation.NodeType},
     * label, formula, and line numbers.
     */
    private DiffNode mergeToGood(final VariationTreeNode before, final VariationTreeNode after) {
        Assert.assertEquals(before.getNodeType(), after.getNodeType());
        Assert.assertEquals(lines.get(before), lines.get(after));
        Assert.assertEquals(before.getFormula(), after.getFormula());
        Assert.assertEquals(before.getLabelLines(), after.getLabelLines());

        final DiffLineNumberRange nlines = lines.get(before);

        return new DiffNode(
                NON,
                before.getNodeType(),
                nlines.from(),
                nlines.to(),
                before.getFormula(),
                before.getLabelLines()
        );
    }

    /**
     * Creates a bad diff from a DiffTree.
     * @param d The DiffTree to convert to a bad diff.
     * @see BadVDiff
     */
    public static BadVDiff fromGood(DiffTree d) {
        record EdgeToConstruct(
                VariationTreeNode child,
                DiffNode parent,
                Time t
        ) {}

        final FromGoodNodeTranslation nodeTranslation = new FromGoodNodeTranslation();

        final Map<VariationTreeNode, VariationTreeNode>   matching = new HashMap<>();
        final Map<VariationTreeNode, DiffType>            coloring = new HashMap<>();
        final Map<VariationTreeNode, DiffLineNumberRange> lines    = new HashMap<>();
        final Set<DiffNode> splittedNodes = new HashSet<>();

        final List<EdgeToConstruct> edgesToConstruct = new ArrayList<>();

        final VariationTreeNode root = fromGood(d.getRoot(), nodeTranslation, coloring, lines);

        d.forAll(diffNode -> {
            if (diffNode == d.getRoot()) {
                return;
            }

            final DiffNode pbefore = diffNode.getParent(BEFORE);
            final DiffNode pafter = diffNode.getParent(AFTER);

            final DiffType color = diffNode.getDiffType();

            final boolean unchanged = color == NON;
            final boolean hasUnsplitUnchangedParent =
                       pbefore == pafter
                    && pbefore != null
                    && pbefore.isNon()
                    && !splittedNodes.contains(pbefore);
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

                final VariationTreeNode selfBefore = plain(diffNode);
                final VariationTreeNode selfAfter  = plain(diffNode);

                nodeTranslation.put(diffNode, BEFORE, selfBefore);
                nodeTranslation.put(diffNode, AFTER,  selfAfter);

                edgesToConstruct.add(new EdgeToConstruct(
                        selfBefore, pbefore, BEFORE
                ));
                edgesToConstruct.add(new EdgeToConstruct(
                        selfAfter, pafter, AFTER
                ));

                // further metadata to copy
                final DiffLineNumberRange dRange = new DiffLineNumberRange(diffNode.getFromLine(), diffNode.getToLine());
                lines.put(selfBefore, dRange);
                lines.put(selfAfter,  dRange);
                coloring.put(selfBefore, REM);
                coloring.put(selfAfter,  ADD);
                matching.put(selfBefore, selfAfter);
                matching.put(selfAfter,  selfBefore);

                splittedNodes.add(diffNode);
            } else {
                final VariationTreeNode self = fromGood(diffNode, nodeTranslation, coloring, lines);

                /*
                 * Else is important in the following branching:
                 * - It does not affect added or removed nodes as these will have only one parent anyway.
                 * - For unchanged nodes, we want to construct only one parent edge in the resulting
                 *   variation tree. In a variation tree, every node has only one parent and when we would
                 *   issue two parallel edges to be constructed here, then we variation tree construction would
                 *   fail since every node can have only one parent.
                 */
                if (pbefore != null) {
                    edgesToConstruct.add(new EdgeToConstruct(
                            self, pbefore, BEFORE
                    ));
                } else if (pafter != null) {
                    edgesToConstruct.add(new EdgeToConstruct(
                            self, pafter, AFTER
                    ));
                }
            }
        });

        for (final EdgeToConstruct e : edgesToConstruct) {
            nodeTranslation.get(e.parent, e.t).addChild(e.child);
        }

        return new BadVDiff(
                new VariationTree(root, new BadVDiffFromDiffTreeSource(d.getSource())),
                matching,
                coloring,
                lines
        );
    }

    /**
     * Inverse of {@link #fromGood(DiffTree)}.
     * Restores the DiffTree that is represented by this bad tree diff.
     */
    public DiffTree toGood() {
        record EdgeToConstruct(
                DiffNode child,
                VariationTreeNode parent,
                Time time
        ) {}

        final List<EdgeToConstruct>            edgesToConstruct = new ArrayList<>();
        final Map<VariationTreeNode, DiffNode> nodeTranslation  = new HashMap<>();

        final DiffNode root = toGood(diff.root());
        nodeTranslation.put(diff.root(), root);

        diff.forAll(v -> {
            // If a node was already translated (because it was merged), it does not have to be translated anymore.
            // We already translated the root, so we can skip it.
            if (nodeTranslation.containsKey(v) || v == diff.root()) {
                return;
            }

            final VariationTreeNode parent = v.getParent();
            Assert.assertNotNull(parent);

            final VariationTreeNode badBuddy = matching.get(v);
            if (badBuddy == null) {
                // v was not cloned.
                // We can just directly convert it to a DiffNode.
                final DiffNode vGood = toGood(v);

                nodeTranslation.put(v, vGood);
                coloring.get(v).forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct(vGood, parent, t))
                );
            } else {
                // v was cloned.
                // We have to merge it with its cloning partner.
                final DiffNode vGood = mergeToGood(v, badBuddy);

                final DiffType vColor = coloring.get(v);
                final DiffType badBuddyColor = coloring.get(badBuddy);
                Assert.assertTrue(vColor.inverse() == badBuddyColor);

                nodeTranslation.put(v, vGood);
                nodeTranslation.put(badBuddy, vGood);

                // Since the colors are ADD and REM, both following calls will only
                // invoke the callback for a single time:
                // BEFORE for REM and AFTER for ADD.
                vColor.forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct(vGood, parent, t))
                );
                badBuddyColor.forAllTimesOfExistence(
                        t -> edgesToConstruct.add(new EdgeToConstruct(vGood, badBuddy.getParent(), t))
                );
            }
        });

        for (final EdgeToConstruct e : edgesToConstruct) {
            nodeTranslation.get(e.parent()).addChild(e.child(), e.time());
        }

        DiffTreeSource source = DiffTreeSource.Unknown;
        if (diff.source() instanceof BadVDiffFromDiffTreeSource s) {
            source = s.initialDiffTree();
        }

        return new DiffTree(root, source);
    }

    private void prettyPrint(final String indent, StringBuilder b, VariationTreeNode n) {
        if (!n.isRoot()) {
            b
                    .append(coloring.get(n).symbol)
                    .append(indent)
                    .append(n.getLabelLines().stream().map(String::trim).collect(Collectors.joining(" \\n ")))
                    .append(StringUtils.LINEBREAK);
        }

        for (VariationTreeNode child : n.getChildren()) {
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
