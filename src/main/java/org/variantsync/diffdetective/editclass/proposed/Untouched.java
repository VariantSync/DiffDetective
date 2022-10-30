package org.variantsync.diffdetective.editclass.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.editclass.EditClass;

import static org.variantsync.diffdetective.diff.difftree.Time.AFTER;
import static org.variantsync.diffdetective.diff.difftree.Time.BEFORE;

/**
 * Our Untouched edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class Untouched extends EditClass {
    Untouched() {
        super("Untouched", DiffType.NON);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        final Node pcb = artifactNode.getFeatureMapping(BEFORE);
        final Node pca = artifactNode.getFeatureMapping(AFTER);
        return SAT.equivalent(pcb, pca) && artifactNode.beforePathEqualsAfterPath();
    }
}
