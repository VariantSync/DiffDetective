package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeVisitor;

import java.util.ArrayList;

/**
 * This transformer removes all subtrees from a DiffTree that are non-edited.
 * A subtree is unedited, if all nodes in it are unchanged and all nodes have the same
 * before and after parent.
 * Such subtrees just model state but not an edit and thus are removed from the validation
 * of our edit classes in our ESEC/FSE'22 paper.
 * @author Paul Bittner
 */
public class CutNonEditedSubtrees implements DiffTreeTransformer, DiffTreeVisitor {
    @Override
    public void transform(final DiffTree diffTree) {
        diffTree.traverse(this);
    }

    @Override
    public void visit(final DiffTreeTraversal traversal, final DiffNode subtree) {
        final ArrayList<DiffNode> collapsableChildren = new ArrayList<>();
        for (final DiffNode child : subtree.getAllChildren()) {
            traversal.visit(child);

            /*
             * Collapse all children c for which
             *   1. all children of c could be collapsed or c never had children
             *   2. that was not relocated due to an edit (beforeparent and afterparent are the same).
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
                    child.getAllChildren().isEmpty()
                            && child.getAfterParent() == subtree
                            && child.getBeforeParent() == subtree)
            {
                collapsableChildren.add(child);
            }
        }

        // ... remove all children.
        if (!collapsableChildren.isEmpty()) {
            subtree.removeChildren(collapsableChildren);
        }
    }

    @Override
    public String toString() {
        return "CutNonEditedSubtrees";
    }
}
