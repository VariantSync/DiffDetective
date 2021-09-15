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
    private final List<Stack<DiffNode>> chains = new ArrayList<>();

    @Override
    public List<Class<? extends DiffTreeTransformer>> getDependencies() {
        return List.of(CollapseNonEditedSubtrees.class);
    }

    @Override
    public void transform(final DiffTree diffTree) {
        DiffTreeTraversal.with(this).visit(diffTree);
        System.out.println(chains);
        for (final Stack<DiffNode> chain : chains) {
            collapseChain(chain, diffTree);
        }
        chains.clear();
    }

    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        if (subtree.isNon() && subtree.isMacro()) {
            if (subtree.getBeforeParent() == subtree.getAfterParent() && !subtree.getBeforeParent().isRoot()) {
                final DiffNode parent = subtree.getBeforeParent();

                for (final Stack<DiffNode> s : chains) {
                    if (s.peek() == parent) {
                        s.push(subtree);
                        break;
                    }
                }
            } else {
                // We found the head of a chain
                Stack<DiffNode> s = new Stack<>();
                s.push(subtree);
                chains.add(s);
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
}
