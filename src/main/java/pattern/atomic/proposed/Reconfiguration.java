package pattern.atomic.proposed;

import analysis.logic.SAT;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import org.prop4j.Node;
import pattern.atomic.AtomicPattern;

final class Reconfiguration extends AtomicPattern {
    Reconfiguration() {
        super("Reconfiguration", DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        final Node pcb = codeNode.getBeforeFeatureMapping();
        final Node pca = codeNode.getAfterFeatureMapping();
        return !SAT.implies(pcb, pca) && !SAT.implies(pca, pcb);
    }
}
