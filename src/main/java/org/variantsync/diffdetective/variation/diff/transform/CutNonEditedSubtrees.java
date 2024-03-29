package org.variantsync.diffdetective.variation.diff.transform;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.traverse.VariationDiffTraversal;
import org.variantsync.diffdetective.variation.diff.traverse.VariationDiffVisitor;

import java.util.ArrayList;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * This transformer removes all subtrees from a VariationDiff that are non-edited.
 * A subtree is unedited, if all nodes in it are unchanged and all nodes have the same
 * before and after parent.
 * Such subtrees just model state but not an edit and thus are removed from the validation
 * of our edit classes in our ESEC/FSE'22 paper.
 * @author Paul Bittner
 */
public class CutNonEditedSubtrees<L extends Label> implements VariationDiffTransformer<L>, VariationDiffVisitor<L> {
    private final boolean keepDummy;

    /**
     * @param keepDummy The transformation will keep the root of any removed subtree if this is true.
     *                  Otherwise the entire subtree is removed.
     *                  Default is false in {@link CutNonEditedSubtrees#CutNonEditedSubtrees()}.
     */
    public CutNonEditedSubtrees(boolean keepDummy) {
        this.keepDummy = keepDummy;
    }

    public CutNonEditedSubtrees() {
        this(false);
    }

    public static <L extends Label> void genericTransform(VariationDiff<L> variationDiff) {
        new CutNonEditedSubtrees<L>().transform(variationDiff);
    }

    public static <L extends Label> void genericTransform(boolean keepDummy, VariationDiff<L> variationDiff) {
        new CutNonEditedSubtrees<L>(keepDummy).transform(variationDiff);
    }

    @Override
    public void transform(final VariationDiff<L> variationDiff) {
        variationDiff.traverse(this);
    }

    @Override
    public void visit(final VariationDiffTraversal<L> traversal, final DiffNode<L> subtree) {
        final ArrayList<DiffNode<L>> collapsableChildren = new ArrayList<>();
        for (final DiffNode<L> child : subtree.getAllChildren()) {
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
                    child.isLeaf()
                            && child.getParent(AFTER) == subtree
                            && child.getParent(BEFORE) == subtree)
            {
                collapsableChildren.add(child);
            }
        }

        // ... remove all children.
        if (!collapsableChildren.isEmpty()) {
            final boolean subtreeIsAlsoCollapsable =
                    (collapsableChildren.size() == subtree.getTotalNumberOfChildren()) // => subtree.getAllChildren().isEmpty() after removing children
                    && subtree.getParent(AFTER) == subtree.getParent(BEFORE) // analogous to child.getParent(AFTER) == subtree && child.getParent(BEFORE) == subtree
                    && subtree.getParent(AFTER) != null; // needed in case we reached the root. If subtree is the root, then it is not collabsable
            if (!keepDummy || subtreeIsAlsoCollapsable) {
                subtree.removeChildren(collapsableChildren);
            }
        }
    }

    @Override
    public String toString() {
        return "CutNonEditedSubtrees";
    }
}
