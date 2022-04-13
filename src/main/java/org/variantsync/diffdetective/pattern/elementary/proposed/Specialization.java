package org.variantsync.diffdetective.pattern.elementary.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.elementary.ElementaryPattern;

final class Specialization extends ElementaryPattern {
    Specialization() {
        super("Specialization", DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        final Node pcb = codeNode.getBeforeFeatureMapping();
        final Node pca = codeNode.getAfterFeatureMapping();
        return !SAT.implies(pcb, pca) && SAT.implies(pca, pcb);
    }
}
