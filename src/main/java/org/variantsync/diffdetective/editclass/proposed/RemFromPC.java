package org.variantsync.diffdetective.editclass.proposed;

import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;

import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Our RemFromPC edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class RemFromPC extends EditClass {
    RemFromPC() {
        super("RemFromPC", DiffType.REM);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        return !artifactNode.getParent(BEFORE).isRem();
    }
}
