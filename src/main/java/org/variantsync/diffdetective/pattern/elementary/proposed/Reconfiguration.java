package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

/**
 * Our Reconfiguration pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class Reconfiguration extends ElementaryPattern {
    Reconfiguration() {
        super("Reconfiguration", DiffType.NON);
    }

    @Override
    protected boolean matchesArtifactNode(DiffNode artifactNode) {
        final Node pcb = artifactNode.getBeforeFeatureMapping();
        final Node pca = artifactNode.getAfterFeatureMapping();
        return !SAT.implies(pcb, pca) && !SAT.implies(pca, pcb);
    }
}
