package org.variantsync.diffdetective.util.fide;

import org.prop4j.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

/**
 * Class to fix bugs related to True and False classes of FeatureIDE.
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1111
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1333
 *
 * This class contains constants for representing atomic values true and false in formulas
 * as well as a conversion method for parsing certain feature names to true and false, respectively.
 */
public class FixTrueFalse {
    /// Names of variables that we want to interpret as atomic values true or false, respectively.
    public final static List<String> TrueNames = Arrays.asList("true", "1");
    public final static List<String> FalseNames = Arrays.asList("false", "0");

    /*
    Constant literals representing the true and false value
     */
    public final static Literal True = new org.prop4j.True();
    public final static Literal False = new org.prop4j.False();

    /**
     * @return True iff the given formula is a true literal.
     * @see FixTrueFalse::isTrueLiteral
     */
    public static boolean isTrue(final Node n) {
        return n instanceof Literal l && isTrueLiteral(l);
    }

    /**
     * @return True iff the given formula is a false literal.
     * @see FixTrueFalse::isFalseLiteral
     */
    public static boolean isFalse(final Node n) {
        return n instanceof Literal l && isFalseLiteral(l);
    }

    /**
     * @return True iff the given name represents the atomic value true w.r.t. the constant TrueNames.
     */
    public static boolean isTrueLiteral(final Literal l) {
        return TrueNames.stream().anyMatch(t -> t.equalsIgnoreCase(l.var.toString()));
    }

    /**
     * @return True iff the given name represents the atomic value false w.r.t. the constant FalseNames.
     */
    public static boolean isFalseLiteral(final Literal l) {
        return FalseNames.stream().anyMatch(f -> f.equalsIgnoreCase(l.var.toString()));
    }

    private static Node[] filterMatches(final Node[] nodes, Predicate<Node> filter) {
        final ArrayList<Node> filtered = new ArrayList<>();
        for (Node n : nodes) {
            if (!filter.test(n)) {
                filtered.add(n);
            }
        }
        return filtered.toArray(Node[]::new);
    }

    private static <T> boolean contains(T[] ts, Predicate<T> elem) {
        for (final T t : ts) {
            if (elem.test(t)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Replaces all literals in the given `formula` with the literals True and False that
     * represent the respective atomic values w.r.t. FixTrueFalse::isTrueLiteral and FixTrueFalse::isFalseLiteral.
     * This e.g. includes replacing literals representing variables with name "1" or "true" with the respective constants.
     * Returns a formula in which the values True and False are eliminated.
     * So you either get True, False, or a formula that does not contain any True or False value.
     * For a non-pure inplace version of this method (which is likely more performant) see {@link FixTrueFalse#EliminateTrueAndFalseInplace(Node)}.
     * @param formula Formula to simplify. It remains unchanged.
     * @return Either True, False, or a formula without True and False.
     */
    public static Node EliminateTrueAndFalse(final Node formula) {
        return EliminateTrueAndFalseInplace(formula.clone());
    }

    /**
     * Same as {@link FixTrueFalse#EliminateTrueAndFalse(Node)} but mutates the given formula inplace.
     * Thus, the given formula should  not be used after invoking this method as it might be corrupted.
     * Instead, the returned node should be used.
     * @return A formula with a consistent representation of true and false values.
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