package diff.difftree.transform;

import diff.difftree.CodeType;
import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.DiffType;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import org.prop4j.And;
import org.prop4j.Node;

import java.util.*;

/**
 * https://scryfall.com/card/2xm/308/wurmcoil-engine
 */
public class CollapseNestedNonEditedMacros implements DiffTreeTransformer, DiffTreeVisitor {
    private final List<Stack<DiffNode>> chainsInBuild = new ArrayList<>();
    private final List<Stack<DiffNode>> finalizedChains = new ArrayList<>();

    @Override
    public List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return List.of(CollapseNonEditedSubtrees.class);
    }

    @Override
    public void transform(final DiffTree diffTree) {
        // find all chains
        DiffTreeTraversal.with(this).visit(diffTree);

        // finalize all remaining in build chains
        while (!chainsInBuild.isEmpty()) {
            finalize(chainsInBuild.get(chainsInBuild.size() - 1));
        }

        // collapse all found chains
        for (final Stack<DiffNode> chain : finalizedChains) {
            collapseChain(chain, diffTree);
        }

        // cleanup
        chainsInBuild.clear();
        finalizedChains.clear();
    }

    private void finalize(Stack<DiffNode> chain) {
        chainsInBuild.remove(chain);
        finalizedChains.add(chain);
    }

    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        if (subtree.isNon() && subtree.isMacro()) {
            if (isHead(subtree)) {
                final Stack<DiffNode> s = new Stack<>();
                s.push(subtree);
                chainsInBuild.add(s);
            } else if (inChainTail(subtree)) {
                final DiffNode parent = subtree.getBeforeParent();

                Stack<DiffNode> pushedTo = null;
                for (final Stack<DiffNode> s : chainsInBuild) {
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

    private static void collapseChain(Stack<DiffNode> chain, DiffTree diffTree) {
        final Collection<DiffNode> children = chain.peek().removeChildren();
        final ArrayList<Node> featureMappings = new ArrayList<>(chain.size());

        DiffNode lastPopped = null;
        assert !chain.isEmpty();
        while (!chain.isEmpty()) {
            lastPopped = chain.pop();
            diffTree.removeFromNodes(lastPopped);

            switch (lastPopped.codeType) {
                case IF ->
                    featureMappings.add(lastPopped.getAfterFeatureMapping());
                case ELSE, ELIF -> {
                    featureMappings.add(lastPopped.getAfterFeatureMapping());
                    // Pop all previous ELIF cases and the final IF (if present) as we accounted
                    // for their features mappings already.
                    while (!lastPopped.isIf() && !chain.isEmpty()) {
                        lastPopped = chain.pop();
                        diffTree.removeFromNodes(lastPopped);
                    }
                }
                case ROOT, CODE ->
                    throw new RuntimeException("Unexpected code type " + lastPopped.codeType + " within macro chain!");
                case ENDIF -> {}
            }
        }

        final DiffNode beforeParent = lastPopped.getBeforeParent();
        final DiffNode afterParent = lastPopped.getAfterParent();

        final DiffNode merged = new DiffNode(
                DiffType.NON, CodeType.IF,
                lastPopped.getFromLine(), lastPopped.getToLine(),
                new And(featureMappings.toArray()),
                "$Collapsed Nested Annotations$");

        lastPopped.drop();
        merged.addChildren(children);
        diffTree.addSubtreeRoot(merged, beforeParent, afterParent);
    }

    /**
     * @return True iff at least one child of was edited.
     */
    private static boolean anyChildEdited(DiffNode d) {
        return d.getChildren().stream().anyMatch(c -> !c.isNon());
    }

    /**
     * @return True iff no child of was edited.
     */
    private static boolean noChildEdited(DiffNode d) {
        return d.getChildren().stream().allMatch(DiffNode::isNon);
    }

    /**
     * @return True iff d is in the tail of a chain.
     */
    private static boolean inChainTail(DiffNode d) {
        return d.getBeforeParent() == d.getAfterParent();
    }

    /**
     * @return True iff d is the head of a chain.
     */
    private static boolean isHead(DiffNode d) {
        return (!inChainTail(d) || d.getBeforeParent().isRoot()) && noChildEdited(d);
    }

    /**
     * @return True iff d is the end of a chain and any chain ending at d has to end.
     */
    private static boolean isEnd(DiffNode d) {
        return inChainTail(d) && anyChildEdited(d);
    }
}
