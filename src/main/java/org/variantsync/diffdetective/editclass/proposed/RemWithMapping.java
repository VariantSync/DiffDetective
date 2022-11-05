package org.variantsync.diffdetective.editclass.proposed;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.editclass.EditClass;

import static org.variantsync.diffdetective.diff.difftree.Time.BEFORE;

/**
 * Our RemWithMapping edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class RemWithMapping extends EditClass {
    RemWithMapping() {
        super("RemWithMapping", DiffType.REM);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        return artifactNode.getParent(BEFORE).isRem();
    }
}
