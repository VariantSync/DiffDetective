package org.variantsync.diffdetective.editclass.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.editclass.EditClass;

import static org.variantsync.diffdetective.diff.difftree.Time.AFTER;

/**
 * Our AddToPC edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class AddToPC extends EditClass {
    AddToPC() {
        super("AddToPC", DiffType.ADD);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode node) {
        return !node.getParent(AFTER).isAdd();
    }
}
