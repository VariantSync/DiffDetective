package org.variantsync.diffdetective.editclass.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.editclass.EditClass;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Our Reconfiguration edit class from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class Reconfiguration extends EditClass {
    Reconfiguration() {
        super("Reconfiguration", DiffType.NON);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode<?> artifactNode) {
        final Node pcb = artifactNode.getPresenceCondition(BEFORE);
        final Node pca = artifactNode.getPresenceCondition(AFTER);
        return !SAT.implies(pcb, pca) && !SAT.implies(pca, pcb);
    }
}
