package org.variantsync.diffdetective.analysis.logic;

import org.prop4j.And;
import org.prop4j.Equals;
import org.prop4j.Node;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FormulaUtils;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * Class with static functions for satisfiability solving, potentially with some optimizations.
 * @author Paul Bittner
 */
public final class SAT {
    private SAT() {}

    /**
     * Invokes a SAT solver on the given formula and returns its result.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
    private static boolean checkSAT(final Node formula) {
        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormula(formula);
        return solver.isSatisfiable();
    }

    /**
     * Checks whether the given formula is satisfiable.
     * This method uses a plain SAT call without using heuristics such as the Tseytin transformation.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
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

    /**
     * Checks whether the given formula is satisfiable.
     * This method uses the Tseytin transformation to transform the formula before SAT solving.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
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

    /**
     * Checks whether the given formula is satisfiable.
     * This method uses the Tseytin transformation for formulas with more than 40 literals as a heuristic to optimize
     * SAT solving times for larger formulas.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
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

    /**
     * Checks whether the given formula is a tautology.
     * @param formula Formula to check for being a tautology.
     * @return True iff the given formula is a tautology.
     */
    public static boolean isTautology(final Node formula) {
        return !isSatisfiable(negate(formula));
    }

    /**
     * Checks whether <code>left</code> =&gt; <code>right</code> is a tautology.
     * This means that the left formula implies the right one for all assignments.
     * @param left Left-hand side propositional formula of implication check.
     * @param right Right-hand side propositional formula of implication check.
     * @return True iff <code>left</code> =&gt; <code>right</code> is a tautology.
     */
    public static boolean implies(final Node left, final Node right) {
        ///   TAUT(left => right)
        /// = TAUT(!left || right)
        /// = !SAT(!(!left || right))
        /// = !SAT(left && !right))
        return !isSatisfiable(new And(left, negate(right)));
    }

    /**
     * Checks whether <code>left</code> &lt;=&gt; <code>right</code> is a tautology.
     * This means that both formula evaluate to the same boolean value for every assignment.
     * @param left Left-hand side propositional formula of equivalency check.
     * @param right Right-hand side propositional formula of equivalency check.
     * @return True iff <code>left</code> &lt;=&gt; <code>right</code> is a tautology.
     */
    public static boolean equivalent(final Node left, final Node right) {
        return isTautology(new Equals(left, right));
    }
}