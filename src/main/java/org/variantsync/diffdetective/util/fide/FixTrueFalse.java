package org.variantsync.diffdetective.util.fide;

import org.prop4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class to fix bugs related to {@link True} and {@link False} of FeatureIDE.
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1111
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1333
 *
 * This class contains constants for representing atomic values true and false in formulas
 * as well as a conversion method for parsing certain feature names to true and false, respectively.
 */
public class FixTrueFalse {
    /** Names of variables that we want to interpret as the constant true. */
    public final static List<String> TrueNames = Arrays.asList("true", "1");
    /** Names of variables that we want to interpret as the constant false. */
    public final static List<String> FalseNames = Arrays.asList("false", "0");

    /** Constant literal representing true. */
    public final static Literal True = new org.prop4j.True();
    /** Constant literal representing false. */
    public final static Literal False = new org.prop4j.False();

    /**
     * Returns {@code true} iff the given formula is a true literal.
     *
     * @see FixTrueFalse#isTrueLiteral
     */
    public static boolean isTrue(final Node n) {
        return n instanceof Literal l && isTrueLiteral(l);
    }

    /**
     * Returns {@code true} iff the given formula is a false literal.
     *
     * @see FixTrueFalse#isFalseLiteral
     */
    public static boolean isFalse(final Node n) {
        return n instanceof Literal l && isFalseLiteral(l);
    }

    /**
     * Returns {@code true} iff the given name represents the atomic value true
     *
     * @see TrueNames
     */
    public static boolean isTrueLiteral(final Literal l) {
        return TrueNames.stream().anyMatch(t -> t.equalsIgnoreCase(l.var.toString()));
    }

    /**
     * Returns {@code true} iff the given name represents the atomic value false
     *
     * @see FalseNames
     */
    public static boolean isFalseLiteral(final Literal l) {
        return FalseNames.stream().anyMatch(f -> f.equalsIgnoreCase(l.var.toString()));
    }

    /**
     * Return a new array only containing elements of {@code nodes} for which the predicate
     * {@code filter} returns false.
     */
    private static Node[] filterMatches(final Node[] nodes, Predicate<Node> filter) {
        final ArrayList<Node> filtered = new ArrayList<>();
        for (Node n : nodes) {
            if (!filter.test(n)) {
                filtered.add(n);
            }
        }
        return filtered.toArray(Node[]::new);
    }

    /** Check if {@code ts} contains an element matching the predicate {@code elem}. */
    private static <T> boolean contains(T[] ts, Predicate<T> elem) {
        for (final T t : ts) {
            if (elem.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces all literals in the given {@code formula} with the literals {@link True} and
     * {@link False}.
     * This includes replacing literals representing variables with names of constants with their
     * actual constant, for example literals with a name contained in {@code TrueNames} by
     * {@link True}. By applying constant folding the result will be either {@link True},
     * {@link False}, or a formula that does not contain any True or False nodes. For an impure,
     * in-place version of this method (which is likely more performant) see
     * {@link EliminateTrueAndFalseInplace}.
     *
     * @param formula the formula to simplify. It remains unchanged.
     * @return either {@link True}, {@link False}, or a formula without True or False
     */
    public static Node EliminateTrueAndFalse(final Node formula) {
        return EliminateTrueAndFalseInplace(formula.clone());
    }

    /**
     * Same as {@link EliminateTrueAndFalse} but mutates the given formula in-place.
     * Thus, the given formula should not be used after invoking this method as it might be
     * corrupted. Instead, the returned node should be used.
     *
     * @param formula the formula to transform
     * @return either {@link True}, {@link False}, or a formula without True or False
     *
     * @see EliminateTrueAndFalse
     */
    public static Node EliminateTrueAndFalseInplace(final Node formula) {
        if (formula instanceof Literal l) {
            if (isTrueLiteral(l)) {
                return l.positive ? True : False;
            }
            if (isFalseLiteral(l)) {
                return l.positive ? False : True;
            }
            return l;
        }

        Node[] children = formula.getChildren();
        for (int i = 0; i < children.length; ++i) {
            children[i] = EliminateTrueAndFalseInplace(children[i]);
        }

        if (formula instanceof And) {
            if (contains(children, FixTrueFalse::isFalse)) {
                return False;
            }
            children = filterMatches(children, FixTrueFalse::isTrue);
            if (children.length == 0) {
                return True;
            }
            if (children.length == 1) {
                return children[0];
            }
        } else if (formula instanceof Or) {
            if (contains(children, FixTrueFalse::isTrue)) {
                return True;
            }
            children = filterMatches(children, FixTrueFalse::isFalse);
            if (children.length == 0) {
                return False;
            }
            if (children.length == 1) {
                return children[0];
            }
        } else {
            if (formula instanceof Not) {
                return FormulaUtils.negate(children[0]);
            } else if (formula instanceof Implies) {
                final Node l = children[0];
                final Node r = children[1];

                if (isFalse(l)) return True;
                if (isFalse(r)) return FormulaUtils.negate(l);
                if (isTrue(l)) return r;
                if (isTrue(r)) return True;
            } else if (formula instanceof Equals) {
                final Node l = children[0];
                final Node r = children[1];

                if (isFalse(l)) return FormulaUtils.negate(r);
                if (isFalse(r)) return FormulaUtils.negate(l);
                if (isTrue(l)) return r;
                if (isTrue(r)) return l;
            }
        }

        formula.setChildren(children);
        return formula;
    }
}
