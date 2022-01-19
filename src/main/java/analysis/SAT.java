package analysis;

import org.prop4j.Equals;
import org.prop4j.Implies;
import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import util.fide.FixTrueFalse;

import static util.fide.FormulaUtils.negate;

public class SAT {
    public static boolean isSatisfiable(Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        // TODO: Remove this block once issue #1333 of FeatureIDE is resolved because FixTrueFalse::On is expensive.
        //       https://github.com/FeatureIDE/FeatureIDE/issues/1333
        {
            formula = FixTrueFalse.EliminateTrueAndFalse(formula);
            if (FixTrueFalse.isTrue(formula)) {
                return true;
            } else if (FixTrueFalse.isFalse(formula)) {
                return false;
            }
        }

        solver.addFormulas(formula);
        return solver.isSatisfiable();
    }

    public static boolean isTautology(final Node formula) {
        return !isSatisfiable(negate(formula));
    }

    public static boolean implies(final Node left, final Node right) {
        return isTautology(new Implies(left, right));
    }

    public static boolean equivalent(final Node left, final Node right) {
        return isTautology(new Equals(left, right));
    }
}