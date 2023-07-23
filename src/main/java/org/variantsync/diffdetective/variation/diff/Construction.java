package org.variantsync.diffdetective.variation.diff;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;

import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.gumtree.DiffTreeAdapter;
import org.variantsync.diffdetective.gumtree.VariationTreeAdapter;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.source.VariationTreeDiffSource;
import org.variantsync.diffdetective.variation.diff.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTree;

import java.util.HashMap;
import java.util.Map;

import static org.variantsync.diffdetective.variation.diff.DiffType.ADD;
import static org.variantsync.diffdetective.variation.diff.DiffType.NON;
import static org.variantsync.diffdetective.variation.diff.DiffType.REM;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class Construction {
    /**
     * Create a {@link DiffTree} by matching nodes between {@code before} and {@code after} with the
     * default GumTree matcher.
     *
     * @see diffUsingMatching(VariationNode, VariationNode, Matcher)
     */
    public static DiffTree diffUsingMatching(VariationTree before, VariationTree after) {
        DiffNode root = diffUsingMatching(
            before.root(),
            after.root(),
            Matchers.getInstance().getMatcher()
        );

        return new DiffTree(root, new VariationTreeDiffSource(before.source(), after.source()));
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
    public static <A extends VariationNode<A>, B extends VariationNode<B>> DiffNode diffUsingMatching(
        VariationNode<A> before,
        VariationNode<B> after,
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
    public static <B extends VariationNode<B>> DiffNode diffUsingMatching(
        DiffNode before,
        VariationNode<B> after,
        Matcher matcher
    ) {
        var src = new DiffTreeAdapter(before, BEFORE);
        var dst = new VariationTreeAdapter(after);

        MappingStore matching = matcher.match(src, dst);
        Assert.assertTrue(matching.has(src, dst));

        removeUnmapped(matching, src);
        for (var child : dst.getChildren()) {
            addUnmapped(matching, src.getDiffNode(), (VariationTreeAdapter)child);
        }

        int[] currentID = new int[1];
        DiffTreeTraversal.forAll((node) -> {
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
    private static void removeUnmapped(MappingStore mappings, DiffTreeAdapter root) {
        for (var node : root.preOrder()) {
            Tree dst = mappings.getDstForSrc(node);
            if (dst == null || !dst.getLabel().equals(node.getLabel())) {
                var diffNode = ((DiffTreeAdapter)node).getDiffNode();
                diffNode.diffType = REM;
                diffNode.drop(AFTER);
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
    private static void addUnmapped(MappingStore mappings, DiffNode parent, VariationTreeAdapter afterNode) {
        VariationNode<?> variationNode = afterNode.getVariationNode();
        DiffNode diffNode;

        Tree src = mappings.getSrcForDst(afterNode);
        if (src == null || !src.getLabel().equals(afterNode.getLabel())) {
            int from = variationNode.getLineRange().fromInclusive();
            int to = variationNode.getLineRange().toExclusive();

            diffNode = new DiffNode(
                ADD,
                variationNode.getNodeType(),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, from, from),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, to, to),
                variationNode.getFormula(),
                variationNode.getLabelLines()
            );
        } else {
            diffNode = ((DiffTreeAdapter)src).getDiffNode();
            if (diffNode.getParent(AFTER) != null) {
                // Always drop and reinsert it because it could have moved.
                diffNode.drop(AFTER);
            }
        }
        parent.addChild(diffNode, AFTER);

        diffNode.removeChildren(AFTER);
        for (var child : afterNode.getChildren()) {
            addUnmapped(mappings, diffNode, (VariationTreeAdapter)child);
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
    public static DiffNode improveMatching(DiffNode tree, Matcher matcher) {
        var src = new DiffTreeAdapter(tree, BEFORE);
        var dst = new DiffTreeAdapter(tree, AFTER);

        MappingStore matching = new MappingStore(src, dst);
        extractMatching(src, dst, matching);
        matcher.match(src, dst, matching);
        Assert.assertTrue(matching.has(src, dst));

        for (var srcNode : src.preOrder()) {
            var dstNode = matching.getDstForSrc(srcNode);
            var beforeNode = ((DiffTreeAdapter)srcNode).getDiffNode();
            if (dstNode == null || !srcNode.getLabel().equals(dstNode.getLabel())) {
                if (beforeNode.isNon()) {
                    splitNode(beforeNode);
                }

                Assert.assertTrue(beforeNode.isRem());
            } else {
                var afterNode = ((DiffTreeAdapter)dstNode).getDiffNode();

                if (beforeNode != afterNode) {
                    if (beforeNode.isNon()) {
                        splitNode(beforeNode);
                    }
                    if (afterNode.isNon()) {
                        afterNode = splitNode(afterNode);
                    }

                    joinNode(beforeNode, afterNode);
                }

                Assert.assertTrue(beforeNode.isNon());
            }
            beforeNode.assertConsistency();
        }

        return tree;
    }

    /**
     * Removes the implicit matching between the {@code BEFORE} and {@code AFTER} projection of
     * {@code beforeNode}. This is achieved by copying {@code beforeNode} and reconnecting all
     * necessary edges such that the new node exists only after and {@code beforeNode} only exists
     * before the edit.
     *
     * This method doesn't change the {@code BEFORE} and {@code AFTER} projection of {@code
     * beforeNode}.
     *
     * @param beforeNode the node to be split
     * @return a copy of {@code beforeNode} existing only after the edit.
     */
    private static DiffNode splitNode(DiffNode beforeNode) {
        Assert.assertTrue(beforeNode.isNon());

        DiffNode afterNode = beforeNode.shallowCopy();

        afterNode.diffType = ADD;
        beforeNode.diffType = REM;

        afterNode.addChildren(beforeNode.removeChildren(AFTER), AFTER);
        var afterParent = beforeNode.getParent(AFTER);
        afterParent.insertChild(afterNode, afterParent.indexOfChild(beforeNode, AFTER), AFTER);
        beforeNode.drop(AFTER);

        beforeNode.assertConsistency();
        afterNode.assertConsistency();

        return afterNode;
    }

    /**
     * Merges {@code afterNode} into {@code beforeNode} such that {@code beforeNode.isNon() ==
     * true}. Essentially, an implicit matching is inserted between {@code beforeNode} and {@code
     * afterNode}.
     *
     * This method doesn't change the {@code BEFORE} and {@code AFTER} projection of {@code
     * beforeNode}.
     *
     * @param beforeNode the node which is will exist {@code BEFORE} and {@code AFTER} the edit
     * @param afterNode the node which is discarded
     */
    private static void joinNode(DiffNode beforeNode, DiffNode afterNode) {
        Assert.assertTrue(beforeNode.isRem());
        Assert.assertTrue(afterNode.isAdd());

        beforeNode.diffType = NON;

        beforeNode.addChildren(afterNode.removeChildren(AFTER), AFTER);

        var afterParent = afterNode.getParent(AFTER);
        afterParent.insertChild(beforeNode, afterParent.indexOfChild(afterNode, AFTER), AFTER);
        afterNode.drop(AFTER);
    }

    /**
     * Makes the implicit matching of a {@code DiffTree} explicit.
     *
     * @param src the source nodes of the matching, must be of the same {@link DiffTree} as {@code
     * dst.
     * @param dst the destination nodes of the matching, must be of the same {@link DiffTree} as
     * {@code src}
     * @param result the destination where the matching between {@code src} and {@code dst} is added
     */
    private static void extractMatching(
        DiffTreeAdapter src,
        DiffTreeAdapter dst,
        MappingStore result
    ) {
        Map<DiffNode, Tree> matching = new HashMap<>();

        for (var srcNode : src.preOrder()) {
            DiffNode diffNode = ((DiffTreeAdapter)srcNode).getDiffNode();
            if (diffNode.isNon()) {
                matching.put(diffNode, srcNode);
            }
        }

        for (var dstNode : dst.preOrder()) {
            DiffNode diffNode = ((DiffTreeAdapter)dstNode).getDiffNode();
            if (diffNode.isNon()) {
                Assert.assertTrue(matching.get(diffNode) != null);
                result.addMapping(matching.get(diffNode), dstNode);
            }
        }
    }
}
