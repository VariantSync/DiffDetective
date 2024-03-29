package org.variantsync.diffdetective.editclass.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Our Untouched edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class Untouched extends EditClass {
    Untouched() {
        super("Untouched", DiffType.NON);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode<?> artifactNode) {
        return artifactNode.beforePathEqualsAfterPath();
    }
}
