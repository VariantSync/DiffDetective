package pattern.atomic.proposed;

import analysis.SAT;
import diff.difftree.DiffNode;
import diff.difftree.DiffType;
import org.prop4j.Node;
import pattern.atomic.AtomicPattern;

final class Refactoring extends AtomicPattern {
    Refactoring() {
        super("Refactoring", DiffType.NON);
    }

    @Override
    protected boolean matchesCodeNode(DiffNode codeNode) {
        final Node pcb = codeNode.getBeforeFeatureMapping();
        final Node pca = codeNode.getAfterFeatureMapping();
        return SAT.equivalent(pcb, pca) && !codeNode.beforePathEqualsAfterPath();
    }
}
