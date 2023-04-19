package org.variantsync.diffdetective.analysis.logic;

import org.prop4j.*;
import org.prop4j.explain.solvers.SatSolver;
import org.prop4j.explain.solvers.SatSolverFactory;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;
import org.variantsync.diffdetective.util.fide.FormulaUtils;

import java.util.HashMap;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * Class with static functions for satisfiability solving, potentially with some optimizations.
 * @author Paul Bittner
 */
public final class SAT {
    private SAT() {}

    public static boolean checkSATviaDNF(final FixTrueFalse.Formula formula) {
        if (formula.isTrueConstant()) {
            return true;
        } else if (formula.isFalseConstant()) {
            return false;
        }

        final Node rdnf = formula.get().toRegularDNF();
        assert rdnf instanceof Or;

        checkClauses : for (final Node clause : rdnf.getChildren()) {
            assert clause instanceof And;

            final HashMap<Object, Boolean> literals = new HashMap<>();

            for (final Node element : clause.getChildren()) {
                final Literal l = (Literal) element;
                final Boolean otherB = literals.putIfAbsent(l.var, l.positive);
                if (otherB != null && otherB != l.positive) {
                    // found two contradicting literals
                    continue checkClauses;
                }
            }

            return true;
        }

        return false;
    }

    /**
     * Invokes a SAT solver on the given formula and returns its result.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
    public static boolean checkSATviaSat4J(final FixTrueFalse.Formula formula) {
        if (formula.isTrueConstant()) {
            return true;
        } else if (formula.isFalseConstant()) {
            return false;
        }

        final SatSolver solver = SatSolverFactory.getDefault().getSatSolver();
        solver.addFormula(formula.get());
        return solver.isSatisfiable();
    }

    /**
     * Checks whether the given formula is satisfiable.
     * This method uses the Tseytin transformation for formulas with more than 40 literals as a heuristic to optimize
     * SAT solving times for larger formulas.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
    public static boolean isSatisfiable(FixTrueFalse.Formula formula) {
        if (formula.isTrueConstant()) {
            return true;
        } else if (formula.isFalseConstant()) {
            return false;
        }

        final int numLiterals = FormulaUtils.numberOfLiterals(formula.get());

        if (numLiterals < 15) {
            return checkSATviaDNF(formula);
        }

        if (numLiterals > 40) {
            formula = formula.mapUnsafe(Tseytin::toEquivalentCNF);
        }

        return checkSATviaSat4J(formula);
    }

    /**
     * Checks whether the given formula is satisfiable.
     * This method uses the Tseytin transformation for formulas with more than 40 literals as a heuristic to optimize
     * SAT solving times for larger formulas.
     * @param formula Formula to check for being satisfiable.
     * @return True iff the given formula is a satisfiable.
     */
    public static boolean isSatisfiable(final Node formula) {
        return isSatisfiable(FixTrueFalse.EliminateTrueAndFalse(formula));
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