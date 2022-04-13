package org.variantsync.diffdetective.analysis.logic;

import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FormulaUtils;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class SAT {
    private static boolean checkSAT(final Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormula(formula);
        return solver.isSatisfiable();
    }

    public static boolean isSatisfiableNoTseytin(Node formula) {
        // TODO: Remove this block once issue #1333 of FeatureIDE is resolved because FixTrueFalse::EliminateTrueAndFalse is expensive.
        //       https://github.com/FeatureIDE/FeatureIDE/issues/1333
        {
            formula = FixTrueFalse.EliminateTrueAndFalse(formula);
            if (FixTrueFalse.isTrue(formula)) {
                return true;
            } else if (FixTrueFalse.isFalse(formula)) {
                return false;
            }
        }
        return checkSAT(formula);
    }

    public static boolean isSatisfiableAlwaysTseytin(Node formula) {
        // TODO: Remove this block once issue #1333 of FeatureIDE is resolved because FixTrueFalse::EliminateTrueAndFalse is expensive.
        //       https://github.com/FeatureIDE/FeatureIDE/issues/1333
        {
            formula = FixTrueFalse.EliminateTrueAndFalse(formula);
            if (FixTrueFalse.isTrue(formula)) {
                return true;
            } else if (FixTrueFalse.isFalse(formula)) {
                return false;
            }
        }

        formula = Tseytin.toEquivalentCNF(formula);

        return checkSAT(formula);
    }

    public static boolean isSatisfiable(Node formula) {
        // TODO: Remove this block once issue #1333 of FeatureIDE is resolved because FixTrueFalse::EliminateTrueAndFalse is expensive.
        //       https://github.com/FeatureIDE/FeatureIDE/issues/1333
        {
            formula = FixTrueFalse.EliminateTrueAndFalse(formula);
            if (FixTrueFalse.isTrue(formula)) {
                return true;
            } else if (FixTrueFalse.isFalse(formula)) {
                return false;
            }
        }

        if (FormulaUtils.numberOfLiterals(formula) > 40) {
            formula = Tseytin.toEquivalentCNF(formula);
        }

        return checkSAT(formula);
    }

    public static boolean isTautology(final Node formula) {
        return !isSatisfiable(negate(formula));
    }

    public static boolean implies(final Node left, final Node right) {
        ///   TAUT(left => right)
//        return isTautology(new Implies(left, right));
        /// = TAUT(!left || right)
//        return isTautology(new Or(negate(left), right));
        /// = !SAT(!(!left || right))
        /// = !SAT(left && !right))
        return !isSatisfiable(new And(left, negate(right)));
    }

    public static boolean equivalent(final Node left, final Node right) {
        return isTautology(new Equals(left, right));
    }
}