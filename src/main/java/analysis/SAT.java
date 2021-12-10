package analysis;

import org.prop4j.*;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;

public class SAT {
    public static boolean isSatisfiable(final Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormulas(formula);
        return solver.isSatisfiable();
    }

    public static boolean isTautology(final Node formula) {
        return !isSatisfiable(negate(formula));
    }

    public static boolean implies(final Node left, final Node right) {
        return isTautology(new Implies(left, right));
    }

    public static boolean equivalent(final Node pc, final And and) {
        return isTautology(new Equals(pc, and));
    }

    public static Node negate(final Node node) {
        if (node instanceof Literal l) {
            return negate(l);
        }

        return new Not(node);
    }

    public static Literal negate(final Literal lit) {
        return new Literal(lit.var, !lit.positive);
    }
}