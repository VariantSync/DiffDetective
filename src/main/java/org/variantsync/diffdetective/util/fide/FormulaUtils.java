package org.variantsync.diffdetective.util.fide;

import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;

/** Utilities for handling {@link Node}s as logical formulas. */
public class FormulaUtils {
    /**
     * Return a new {@link Node} representing the negation of {@code node}.
     * The result will share as much objects with {@code node} possible without modifying
     * {@code node}. As a special case {@link Literal}s are inverted directly without adding a new
     * node.
     */
    public static Node negate(final Node node) {
        if (node instanceof Literal l) {
            return negate(l);
        }

        return new Not(node);
    }

    /** Return a negated copy of {@code lit} without adding a new {@link Node}. */
    public static Literal negate(final Literal lit) {
        if (FixTrueFalse.isTrueLiteral(lit)) {
            return FixTrueFalse.False;
        }
        if (FixTrueFalse.isFalseLiteral(lit)) {
            return FixTrueFalse.True;
        }
        return new Literal(lit.var, !lit.positive);
    }

    /** Recursively counts the number of instances of {@link Literal} in {@code formula}. */
    public static int numberOfLiterals(final Node formula) {
        if (formula instanceof Literal) {
            return 1;
        }

        int sum = 0;
        for (final Node child : formula.getChildren()) {
            sum += numberOfLiterals(child);
        }
        return sum;
    }
}
