package diff.difftree.transform;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class CollapseNonEditedSubtrees implements DiffTreeTransformer, DiffTreeVisitor {
    private List<DiffNode> removedNodes;

    @Override
    public void transform(DiffTree diffTree) {
        removedNodes = new ArrayList<>();
        diffTree.traverse(this);
        diffTree.getAnnotationNodes().removeAll(removedNodes);
        diffTree.getCodeNodes().removeAll(removedNodes);
        removedNodes = null;
    }

    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        final Set<DiffNode> collapsableChildren = new HashSet<>();
        for (final DiffNode child : subtree.getChildren()) {
            traversal.visit(child);

            /*
             * Collapse all children c for which
             *   1. all children of c could be collapsed or c never had children
             *   2. that was not relocated due to an edit.
             *
             * Note: c satisfies 2 => c.isNon() and subtree.isNon()
             *       We thus only cut subtrees that (a) were not edited themselves and (b) were not relocated.
             * Proof: c satisfies 2
             *        => c has an after parent and a before parent
             *        => c exists before and after the edit
             *        => !c.isAdd() && !c.isRem()
             *        => c.isNon()
             *
             *        c satisfies 2
             *        => c has s as after parent and before parent
             *        => s has a before child and an after child
             *        => s exists before and after the edit
             *        => !s.isAdd() && !s.isRem()
             *        => s.isNon()
             */
            if (
                    child.getChildren().isEmpty()
                            && child.getAfterParent() == subtree
                            && child.getBeforeParent() == subtree) {
                collapsableChildren.add(child);
            }
        }

        // ... remove all children.
        if (!collapsableChildren.isEmpty()) {
            removedNodes.addAll(collapsableChildren);
            subtree.removeChildren(collapsableChildren);
        }
    }
}
