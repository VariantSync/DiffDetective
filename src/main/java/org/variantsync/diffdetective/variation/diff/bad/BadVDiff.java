package org.variantsync.diffdetective.variation.diff.bad;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.VariationTreeNode;
import org.variantsync.functjonal.error.NotImplementedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
    private static class NodeTranslation {
        private final Map<DiffNode, Map<Time, VariationTreeNode>> translate = new HashMap<>();

        void put(DiffNode d, VariationTreeNode v) {
            Time.forAll(t -> {
                if (d.getDiffType().existsAtTime(t)) {
                    put(d, t, v);
                }
            });
        }
        void put(DiffNode d, Time t, VariationTreeNode v) {
            translate.putIfAbsent(d, new HashMap<>());
            translate.get(d).put(t, v);
        }


        VariationTreeNode get(DiffNode d, Time t) {
            return translate.getOrDefault(d, new HashMap<>()).get(t);
        }
    }

    private record EdgeToConstruct(
            VariationTreeNode child,
            DiffNode parent,
            Time t
    ) {}

    private static VariationTreeNode plain(final DiffNode n) {
        return new VariationTreeNode(
                n.getNodeType(),
                n.getFormula(),
                n.getLinesInDiff(),
                n.getLabelLines()
        );
    }

    private static VariationTreeNode fromGood(
            final DiffNode n,
            final NodeTranslation nodeTranslation,
            final Map<VariationTreeNode, DiffType> coloring,
            final Map<VariationTreeNode, DiffLineNumberRange> lines) {
        final VariationTreeNode result = plain(n);
        nodeTranslation.put(n, result);
        coloring.put(result, n.getDiffType());
        lines.put(result, new DiffLineNumberRange(n.getFromLine(), n.getToLine()));
        return result;
    }

    public static BadVDiff fromGood(DiffTree d) {
        final NodeTranslation nodeTranslation = new NodeTranslation();

        final Map<VariationTreeNode, VariationTreeNode>   matching = new HashMap<>();
        final Map<VariationTreeNode, DiffType>            coloring = new HashMap<>();
        final Map<VariationTreeNode, DiffLineNumberRange> lines    = new HashMap<>();

        final List<EdgeToConstruct> edgesToConstruct = new ArrayList<>();

        final VariationTreeNode root = fromGood(d.getRoot(), nodeTranslation, coloring, lines);

        d.forAll(diffNode -> {
            if (diffNode == d.getRoot()) {
                return;
            }

            final DiffNode pbefore = diffNode.getParent(BEFORE);
            final DiffNode pafter  = diffNode.getParent(AFTER);

            switch (diffNode.getDiffType()) {
                case ADD, REM -> {
                    Assert.assertTrue(pbefore == null || pafter == null);

                    final VariationTreeNode self = fromGood(diffNode, nodeTranslation, coloring, lines);

                    if (pbefore != null) {
                        edgesToConstruct.add(new EdgeToConstruct(
                                self, pbefore, BEFORE
                        ));
                    }
                    if (pafter != null) {
                        edgesToConstruct.add(new EdgeToConstruct(
                                self, pafter, AFTER
                        ));
                    }
                }
                case NON -> {
                    Assert.assertTrue(pbefore != null && pafter != null);

                    final VariationTreeNode selfBefore = plain(diffNode);
                    final VariationTreeNode selfAfter = plain(diffNode);

                    nodeTranslation.put(diffNode, BEFORE, selfBefore);
                    nodeTranslation.put(diffNode,  AFTER, selfAfter);

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
                    coloring.put(selfBefore, DiffType.REM);
                    coloring.put(selfAfter,  DiffType.ADD);
                    matching.put(selfBefore, selfAfter);
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

    public DiffTree toGood() {
        throw new NotImplementedException();
    }

    private void prettyPrint(final String indent, StringBuilder b, VariationTreeNode n) {
        b.append(indent).append(coloring.get(n).symbol).append(String.join(" \\n ", n.getLabelLines())).append(StringUtils.LINEBREAK);
        for (VariationTreeNode child : n.getChildren()) {
            prettyPrint("  " + indent, b, child);
        }
    }

    public String prettyPrint() {
        final StringBuilder b = new StringBuilder();
        prettyPrint("", b, diff.root());
        return b.toString();
    }
}
