package org.variantsync.diffdetective.variation.diff.transform;

import org.prop4j.And;
import org.prop4j.Node;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.traverse.DiffTreeTraversal;
import org.variantsync.functjonal.Cast;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.NodeType;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Collapses chains of nested non-edited annotations.
 * Imagine a annotation node that is unchanged and has the same parent before and after the edit
 * that is again an unchanged annotation node that has the same parent before and after the edit,
 * and so on. Such chains <code>NON_IF -> NON_IF -> NON_IF -> ... -> NON_IF</code> can be collapsed
 * into a single unchanged annotation node with the formulas of all nodes combined (by AND).
 * This collapse is realized by this transformer.
 *
 * Fun fact: We implemented this transformation because of the
 * <a href="https://scryfall.com/card/2xm/308/wurmcoil-engine">wurmcoil edit in Marlin</a>.
 *
 * @author Paul Bittner
 */
public class CollapseNestedNonEditedAnnotations implements DiffTreeTransformer<DiffLinesLabel> {
    private final List<Stack<DiffNode<DiffLinesLabel>>> chainCandidates = new ArrayList<>();
    private final List<Stack<DiffNode<DiffLinesLabel>>> chains = new ArrayList<>();

    @Override
    public List<Class<? extends DiffTreeTransformer<DiffLinesLabel>>> getDependencies() {
        return List.of(Cast.unchecked(CutNonEditedSubtrees.class));
    }

    @Override
    public void transform(final DiffTree<DiffLinesLabel> diffTree) {
        // find all chains
        diffTree.traverse(this::findChains);

        // Ignore unfinished chainCandidates.
        // For all these chains, no end was found, so they should not be extracted.
        chainCandidates.clear();

        // All found chains should at least have size 2.
        for (final Stack<DiffNode<DiffLinesLabel>> chain : chains) {
            Assert.assertTrue(chain.size() >= 2);
        }

//        System.out.println(StringUtils.prettyPrintNestedCollections(chains));

        // collapse all found chains
        for (final Stack<DiffNode<DiffLinesLabel>> chain : chains) {
            collapseChain(chain);
        }

        // cleanup
        chains.clear();
    }

    private void finalize(Stack<DiffNode<DiffLinesLabel>> chain) {
        chainCandidates.remove(chain);
        chains.add(chain);
    }

    private void findChains(DiffTreeTraversal<DiffLinesLabel> traversal, DiffNode<DiffLinesLabel> subtree) {
        if (subtree.isNon() && subtree.isAnnotation()) {
            if (isHead(subtree)) {
                final Stack<DiffNode<DiffLinesLabel>> s = new Stack<>();
                s.push(subtree);
                chainCandidates.add(s);
            } else if (inChainTail(subtree)) {
                final DiffNode<DiffLinesLabel> parent = subtree.getParent(BEFORE); // == after parent

                Stack<DiffNode<DiffLinesLabel>> pushedTo = null;
                for (final Stack<DiffNode<DiffLinesLabel>> s : chainCandidates) {
                    if (s.peek() == parent) {
                        s.push(subtree);
                        pushedTo = s;
                        break;
                    }
                }

                if (pushedTo != null && isEnd(subtree)) {
                    finalize(pushedTo);
                }
            }
        }

        traversal.visitChildrenOf(subtree);
    }

    private static void collapseChain(Stack<DiffNode<DiffLinesLabel>> chain) {
        final DiffNode<DiffLinesLabel> end = chain.peek();
        final DiffNode<DiffLinesLabel> head = chain.firstElement();
        final ArrayList<Node> featureMappings = new ArrayList<>(chain.size());

        DiffNode<DiffLinesLabel> lastPopped = null;
        Assert.assertTrue(!chain.isEmpty());
        while (!chain.isEmpty()) {
            lastPopped = chain.pop();

            switch (lastPopped.getNodeType()) {
                case IF ->
                    featureMappings.add(lastPopped.getFeatureMapping(AFTER));
                case ELSE, ELIF -> {
                    featureMappings.add(lastPopped.getFeatureMapping(AFTER));
                    // Pop all previous ELIF cases and the final IF (if present) as we accounted
                    // for their features mappings already.
                    while (!lastPopped.isIf() && !chain.isEmpty()) {
                        lastPopped = chain.pop();
                    }
                }
                case ARTIFACT ->
                    throw new RuntimeException("Unexpected node type " + lastPopped.getNodeType() + " within annotation chain!");
            }
        }

        Assert.assertTrue(head == lastPopped);

        final DiffNode<DiffLinesLabel> beforeParent = head.getParent(BEFORE);
        final DiffNode<DiffLinesLabel> afterParent = head.getParent(AFTER);

        var label = new DiffLinesLabel();
        label.addDiffLine(DiffLinesLabel.Line.withInvalidLineNumber("$Collapsed Nested Annotations$"));
        final var merged = new DiffNode<>(
                DiffType.NON, NodeType.IF,
                head.getFromLine(), head.getToLine(),
                new And(featureMappings.toArray()),
                label);

        head.drop();
        merged.stealChildrenOf(end);
        merged.addBelow(beforeParent, afterParent);
    }

    /**
     * @return True iff at least one child of was edited.
     */
    private static boolean anyChildEdited(DiffNode<?> d) {
        return d.getAllChildrenStream().anyMatch(c -> !c.isNon());
    }

    /**
     * @return True iff no child of was edited.
     */
    private static boolean noChildEdited(DiffNode<?> d) {
        return d.getAllChildrenStream().allMatch(DiffNode::isNon);
    }

    private static boolean hasExactlyOneChild(DiffNode<?> d) {
        return d.getAllChildrenStream().limit(2).count() == 1;
    }

    /**
     * @return True iff d is in the tail of a chain.
     */
    private static boolean inChainTail(DiffNode<?> d) {
        return d.getParent(BEFORE) == d.getParent(AFTER) && hasExactlyOneChild(d.getParent(BEFORE));
    }

    /**
     * @return True iff d is the head of a chain.
     */
    private static boolean isHead(DiffNode<?> d) {
        return (!inChainTail(d) || d.getParent(BEFORE).isRoot()) && !isEnd(d);
    }

    /**
     * @return True iff d is the end of a chain and any chain ending at d has to end.
     */
    private static boolean isEnd(DiffNode<?> d) {
        return inChainTail(d) && (anyChildEdited(d) || !hasExactlyOneChild(d));
    }

    @Override
    public String toString() {
        return "CollapseNestedNonEditedAnnotations";
    }
}
