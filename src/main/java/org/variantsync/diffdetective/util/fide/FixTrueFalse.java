package org.variantsync.diffdetective.util.fide;

import org.prop4j.*;
import org.variantsync.diffdetective.util.Assert;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

/**
 * Class to fix bugs related to {@link True} and {@link False} of FeatureIDE.
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1111
 * See: https://github.com/FeatureIDE/FeatureIDE/issues/1333
 *
 * This class contains constants for representing atomic values true and false in formulas
 * as well as a conversion method for parsing certain feature names to true and false, respectively.
 */
public class FixTrueFalse {
    /**
     * This class is a witness that a formula had its true and false constants eliminated
     * or is just such a constant (and nothing else except from perhaps negations).
     */
    public static class Formula {
        private final Node formula;

        private Formula(final Node f) {
            this.formula = f;
        }

        public Node get() {
            return formula;
        }

        public static Node[] gets(final Formula[] formulas) {
            final Node[] cs = new Node[formulas.length];

            for (int i = 0; i < formulas.length; ++i) {
                cs[i] = formulas[i].get();
            }

            return cs;
        }

        /**
         * Runs the given function on this formula and assumes that the given function
         * does not introduce the constants "true" or "false".
         * This method is side-effect free: The transformed formula will be returned but this object remains unaltered.
         * Warning: Use at your own risk! Introducing constants here can lead to wrong SAT results.
         * @param f The function to apply to this formula.
         * @return The converted formula.
         */
        public Formula mapUnsafe(final Function<Node, Node> f) {
            return new Formula(f.apply(formula));
        }

        public static Formula var(final String name) {
            Assert.assertFalse(isTrueLiteral(name) || isFalseLiteral(name));
            return new Formula(new Literal(name, true));
        }

        public static Formula not(final Formula formula) {
            return new Formula(negate(formula.get()));
        }

        public static Formula and(final Formula... formulas) {
            return new Formula(new And(gets(formulas)));
        }

        public static Formula or(final Formula... formulas) {
            return new Formula(new Or(gets(formulas)));
        }

        /**
         * @see FixTrueFalse#isTrue(Node)
         */
        public boolean isTrueConstant() {
            return FixTrueFalse.isTrue(formula);
        }


        /**
         * @see FixTrueFalse#isFalse(Node)
         */
        public boolean isFalseConstant() {
            return FixTrueFalse.isFalse(formula);
        }
    }

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
    public static boolean isTrue(final Node f) {
        return (f instanceof Literal l && isTrueLiteral(l)) || (f instanceof Not n && isFalse(n.getChildren()[0]));
    }

    /**
     * Returns {@code true} iff the given formula is a false literal.
     *
     * @see FixTrueFalse#isFalseLiteral
     */
    public static boolean isFalse(final Node f) {
        return (f instanceof Literal l && isFalseLiteral(l)) || (f instanceof Not n && isTrue(n.getChildren()[0]));
    }

    /**
     * Returns true iff the given literal's variable is the atomic value {@code true}.
     *
     * @see #TrueNames
     */
    public static boolean isTrueLiteral(final Literal l) {
        return isTrueLiteral(l.var.toString());
    }

    /**
     * Returns true iff the given name represents the atomic value {@code true}.
     *
     * @see #TrueNames
     */
    public static boolean isTrueLiteral(final String l) {
        return TrueNames.stream().anyMatch(t -> t.equalsIgnoreCase(l));
    }

    /**
     * Returns true iff the given literal's variable is the atomic value {@code false}.
     *
     * @see #FalseNames
     */
    public static boolean isFalseLiteral(final Literal l) {
        return isFalseLiteral(l.var.toString());
    }

    /**
     * Returns true iff the given name represents the atomic value {@code false}.
     *
     * @see #TrueNames
     */
    public static boolean isFalseLiteral(final String l) {
        return FalseNames.stream().anyMatch(f -> f.equalsIgnoreCase(l));
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
     * {@link #EliminateTrueAndFalseInplace}.
     *
     * @param formula the formula to simplify. It remains unchanged.
     * @return either {@link True}, {@link False}, negations of the previous
     *         (i.e., in terms of {@link Not} or {@link Literal}), or a formula without True or False
     */
    public static Formula EliminateTrueAndFalse(final Node formula) {
        return EliminateTrueAndFalseInplace(formula.clone());
    }

    /**
     * Same as {@link #EliminateTrueAndFalse} but mutates the given formula in-place.
     * Thus, the given formula should not be used after invoking this method as it might be
     * corrupted. Instead, the returned node should be used.
     *
     * @param formula the formula to transform
     * @return either {@link True}, {@link False}, negations of the previous
     *         (i.e., in terms of {@link Not} or {@link Literal}), or a formula without True or False
     *
     * @see #EliminateTrueAndFalse
     */
    public static Formula EliminateTrueAndFalseInplace(final Node formula) {
        return new Formula(EliminateTrueAndFalseInplaceRecurse(formula));
    }

    private static Node EliminateTrueAndFalseInplaceRecurse(final Node formula) {
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
            children[i] = EliminateTrueAndFalseInplaceRecurse(children[i]);
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
                return negate(children[0]);
            } else if (formula instanceof Implies) {
                final Node l = children[0];
                final Node r = children[1];

                if (isFalse(l)) return True;
                if (isFalse(r)) return negate(l);
                if (isTrue(l)) return r;
                if (isTrue(r)) return True;
            } else if (formula instanceof Equals) {
                final Node l = children[0];
                final Node r = children[1];

                if (isFalse(l)) return negate(r);
                if (isFalse(r)) return negate(l);
                if (isTrue(l)) return r;
                if (isTrue(r)) return l;
            }
        }

        formula.setChildren(children);
        return formula;
    }
}
