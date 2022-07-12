package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

/**
 * Our Untouched pattern from the ESEC/FSE'22 paper.
 * @author Paul Bittner, Sören Viegener
 */
final class Untouched extends ElementaryPattern {
    Untouched() {
        super("Untouched", DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        final Node pcb = codeNode.getBeforeFeatureMapping();
        final Node pca = codeNode.getAfterFeatureMapping();
        return SAT.equivalent(pcb, pca) && codeNode.beforePathEqualsAfterPath();
    }
}