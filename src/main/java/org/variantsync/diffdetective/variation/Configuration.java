package org.variantsync.diffdetective.variation;

import java.util.Map;

import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;

public record Configuration(Map<Object, Boolean> assingments) {
    public boolean includes(Node presenceCondition) {
        // TODO FixTrueFalse (analysis.logic.SAT)
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormula(presenceCondition);
        solver.addAssumptions(assingments());
        return solver.isSatisfiable();
    }

    public Node toFormula() {
        Object[] literals = new Object[assingments().size()];

        int index = 0;
        for (var assingment : assingments().entrySet()) {
            literals[index++] = new Literal(assignment.getKey(), assignment.getValue());
        }

        return new And(literals);
    }
}
