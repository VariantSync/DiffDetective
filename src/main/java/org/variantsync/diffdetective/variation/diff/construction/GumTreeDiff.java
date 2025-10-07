package org.variantsync.diffdetective.variation.diff.construction;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.gumtree.VariationDiffAdapter;
import org.variantsync.diffdetective.gumtree.VariationTreeAdapter;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.source.VariationTreeDiffSource;
import org.variantsync.diffdetective.variation.diff.traverse.VariationDiffTraversal;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.functjonal.Cast;

import java.util.HashMap;
import java.util.Map;

import static org.variantsync.diffdetective.variation.diff.DiffType.ADD;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class GumTreeDiff {
    /**
     * Create a {@link VariationDiff} by matching nodes between {@code before} and {@code after} with the
     * default GumTree matcher.
     *
     * @see diffUsingMatching(VariationNode, VariationNode, Matcher)
     */
    public static <L extends Label> VariationDiff<L> diffUsingMatching(VariationTree<L> before, VariationTree<L> after) {
        return diffUsingMatching(before, after, Matchers.getInstance().getMatcher());
    }

    /**
     * Create a {@link VariationDiff} by matching nodes between {@code before} and {@code after}
     * with {@code matcher}.
     */
    public static <L extends Label> VariationDiff<L> diffUsingMatching(VariationTree<L> before, VariationTree<L> after, Matcher matcher) {
        DiffNode<L> root = diffUsingMatching(
            before.root(),
            after.root(),
            matcher
        );

        return new VariationDiff<>(root, new VariationTreeDiffSource(before.source(), after.source()));
    }

    /**
     * Create a {@link DiffNode} by matching nodes between {@code before} and {@code after} with
     * {@code matcher}. The arguments of this function aren't modified (note the
     * {@link diffUsingMatching(DiffNode, VariationNode, Matcher) overload} which modifies
     * {@code before} in-place.
     *
     * @param before the variation tree before an edit
     * @param after the variation tree after an edit
     * @see diffUsingMatching(DiffNode, VariationNode, Matcher)
     */
    public static <A extends VariationNode<A, L>, B extends VariationNode<B, L>, L extends Label> DiffNode<L> diffUsingMatching(
        VariationNode<A, L> before,
        VariationNode<B, L> after,
        Matcher matcher
    ) {
        return diffUsingMatching(DiffNode.unchanged(before), after, matcher);
    }

    /**
     * Create a {@link DiffNode} by matching nodes between {@code before} and {@code after} with
     * {@code matcher}. The result of this function is {@code before} which is modified in-place. In
     * contrast, {@code after} is kept in tact.
     *
     * Warning: Modifications to {@code before} shouldn't concurrently modify {@code after}.
     *
     * Note: There are currently no guarantees about the line numbers. But it is guaranteed that
     * {@link DiffNode#getID} is unique.
     *
     * @param before the variation tree before an edit
     * @param after the variation tree after an edit
     * @see "Constructing Variation Diffs Using Tree Diffing Algorithms"
     */
    public static <B extends VariationNode<B, L>, L extends Label> DiffNode<L> diffUsingMatching(
        DiffNode<L> before,
        VariationNode<B, L> after,
        Matcher matcher
    ) {
        var src = new VariationDiffAdapter<L>(before, BEFORE);
        var dst = new VariationTreeAdapter<L>(after);

        MappingStore matching = matcher.match(src, dst);

        // The following algorithm assumes that the root nodes are matched so we ensure that this is
        // the case here by establishing that mapping if necessary.
        ensureMapping(matching, src, dst);

        removeUnmapped(matching, src);
        for (var child : dst.getChildren()) {
            addUnmapped(matching, src.getDiffNode(), Cast.unchecked(child));
        }

        int[] currentID = new int[1];
        VariationDiffTraversal.<L>forAll((node) -> {
            node.setFromLine(node.getFromLine().withLineNumberInDiff(currentID[0]));
            node.setToLine(node.getToLine().withLineNumberInDiff(currentID[0]));
            ++currentID[0];
        }).visit(before);

        return before;
    }

    /**
     * Remove all nodes from the {@code BEFORE} projection which aren't part of a mapping.
     *
     * @param mappings the matching between the {@code BEFORE} projection of {@code root} some
     * variation tree
     * @param root the variation diff whose before projection is modified
     */
    private static <L extends Label> void removeUnmapped(MappingStore mappings, VariationDiffAdapter<L> root) {
        for (var node : root.preOrder()) {
            Tree dst = mappings.getDstForSrc(node);
            if (dst == null || !dst.getLabel().equals(node.getLabel())) {
                var diffNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(node).getDiffNode();
                diffNode.split(AFTER).drop();
            }
        }
    }

    /**
     * Recursively adds {@code afterNode} to {@code parent} reusing matched nodes.
     *
     * The variation diff {@code parent} is modified in-place such that its {@code AFTER}
     * projection contains a child equivalent to {@code afterNode} which shares matched nodes with
     * the {@code BEFORE} projection of {@code parent}.
     *
     * @param mappings the matching between the {@code BEFORE} projection of {@code root} and a
     * variation tree containing {@code afterNode}
     * @param parent the variation diff whose {@code AFTER} projection is modified
     * @param afterNode a desired child of {@code parent}'s {@code AFTER} projection
     */
    private static <L extends Label> void addUnmapped(MappingStore mappings, DiffNode<L> parent, VariationTreeAdapter<L> afterNode) {
        VariationNode<?, L> variationNode = afterNode.getVariationNode();
        DiffNode<L> diffNode;

        Tree src = mappings.getSrcForDst(afterNode);
        if (src == null || !src.getLabel().equals(afterNode.getLabel())) {
            int from = variationNode.getLineRange().fromInclusive();
            int to = variationNode.getLineRange().toExclusive();

            diffNode = new DiffNode<L>(
                ADD,
                variationNode.getNodeType(),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, from, from),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, to, to),
                variationNode.getFormula(),
                Cast.unchecked(variationNode.getLabel().withoutTimeDependentState(BEFORE))
            );
        } else {
            diffNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(src).getDiffNode();
            if (diffNode.getParent(AFTER) != null) {
                // Always drop and reinsert it because it could have moved.
                diffNode.drop(AFTER);
            }

            diffNode.setFromLine(diffNode.getFromLine().withLineNumberAtTime(afterNode.getVariationNode().getLineRange().fromInclusive(), AFTER));
            diffNode.setToLine(diffNode.getToLine().withLineNumberAtTime(afterNode.getVariationNode().getLineRange().toExclusive(), AFTER));
            diffNode.setLabel(Cast.unchecked(diffNode.getLabel().withTimeDependentStateFrom(afterNode.getVariationNode().getLabel(), Time.AFTER)));
        }
        parent.addChild(diffNode, AFTER);

        diffNode.removeChildren(AFTER);
        for (var child : afterNode.getChildren()) {
            addUnmapped(mappings, diffNode, Cast.unchecked(child));
        }
    }

    /**
     * Run {@code matcher} on the matching extracted from {@code tree} and modify {@code tree}
     * in-place to reflect the new matching.
     *
     * This is equivalent to {@code diffUsingMatching} except that the existing implicit matching
     * is {@link extractMatching extracted} and used as basis for the new matching. Hence, this
     * method is mostly an optimisation to avoid a copy of the {@code AFTER} projection of {@code
     * tree}.
     *
     * @see "Constructing Variation Diffs Using Tree Diffing Algorithms"
     */
    public static <L extends Label> DiffNode<L> improveMatching(DiffNode<L> tree, Matcher matcher) {
        var src = new VariationDiffAdapter<L>(tree, BEFORE);
        var dst = new VariationDiffAdapter<L>(tree, AFTER);

        MappingStore matching = new MappingStore(src, dst);
        extractMatching(src, dst, matching);
        matcher.match(src, dst, matching);

        // The following algorithm assumes that the root nodes are matched so we ensure that this is
        // the case here by establishing that mapping if necessary.
        ensureMapping(matching, src, dst);

        for (var srcNode : src.preOrder()) {
            var dstNode = matching.getDstForSrc(srcNode);
            var beforeNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(srcNode).getDiffNode();
            if (dstNode == null || !srcNode.getLabel().equals(dstNode.getLabel())) {
                if (beforeNode.isNon()) {
                    beforeNode.split(AFTER);
                }

                Assert.assertTrue(beforeNode.isRem());
            } else {
                var afterNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(dstNode).getDiffNode();

                if (beforeNode != afterNode) {
                    if (beforeNode.isNon()) {
                        beforeNode.split(AFTER);
                    }
                    if (afterNode.isNon()) {
                        afterNode.split(BEFORE);
                    }

                    beforeNode.join(afterNode);
                }

                Assert.assertTrue(beforeNode.isNon());
            }
            beforeNode.assertConsistency();
        }

        return tree;
    }

    /**
     * Makes the implicit matching of a {@code VariationDiff} explicit.
     *
     * @param src the source nodes of the matching, must be of the same {@link VariationDiff} as {@code dst}.
     * @param dst the destination nodes of the matching, must be of the same {@link VariationDiff} as {@code src}
     * @param result the destination where the matching between {@code src} and {@param dst} is added.
     */
    private static <L extends Label> void extractMatching(
        VariationDiffAdapter<L> src,
        VariationDiffAdapter<L> dst,
        MappingStore result
    ) {
        Map<DiffNode<L>, Tree> matching = new HashMap<>();

        for (var srcNode : src.preOrder()) {
            DiffNode<L> diffNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(srcNode).getDiffNode();
            if (diffNode.isNon()) {
                matching.put(diffNode, srcNode);
            }
        }

        for (var dstNode : dst.preOrder()) {
            DiffNode<L> diffNode = Cast.<Tree, VariationDiffAdapter<L>>unchecked(dstNode).getDiffNode();
            if (diffNode.isNon()) {
                Assert.assertTrue(matching.get(diffNode) != null);
                result.addMapping(matching.get(diffNode), dstNode);
            }
        }
    }

    /**
     * Add a mapping between {@code src} and {@code dst}.
     * In case {@code src} or {@code dst} are mapped to some other nodes, these mappings are
     * removed.
     */
    private static void ensureMapping(MappingStore matching, Tree src, Tree dst) {
        if (matching.isSrcMapped(src)) {
            matching.removeMapping(src, matching.getDstForSrc(src));
        }
        if (matching.isDstMapped(dst)) {
            matching.removeMapping(dst, matching.getSrcForDst(dst));
        }

        matching.addMapping(src, dst);
    }
}
