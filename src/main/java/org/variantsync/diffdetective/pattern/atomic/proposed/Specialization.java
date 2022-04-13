package org.variantsync.diffdetective.pattern.atomic.proposed;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffType;
import org.variantsync.diffdetective.pattern.atomic.AtomicPattern;

final class Specialization extends AtomicPattern {
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
