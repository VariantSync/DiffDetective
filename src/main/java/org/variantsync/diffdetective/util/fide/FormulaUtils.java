package org.variantsync.diffdetective.util.fide;

import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.prop4j.Not;
import org.tinylog.Logger;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.experiments.views.ViewAnalysis;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.functjonal.Cast;

import java.util.*;

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

    public static Literal var(final String name) {
        return new Literal(name, true);
    }

    public static And and(Node... nodes) {
        return new And(nodes);
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


    public static void sortRegularCNF(final Node rcnf) {
        Assert.assertTrue(rcnf instanceof And);

        // sort literals in clauses by string compare
        Node[] cs = rcnf.getChildren();
        for (final Node c : cs) {
            Arrays.sort(c.getChildren(), (e1, e2) -> {
                final Literal l1 = Cast.unchecked(e1);
                final Literal l2 = Cast.unchecked(e2);
                return ((String) l1.var).compareTo((String) l2.var);
            });
        }

        // sort clauses by literal count
        // clauses with equal literal count will be sorted by their literals as string (might be useless)
        Arrays.sort(cs, Comparator
                .comparingInt((Node e) -> e.getChildren().length)
                .thenComparing(e -> Arrays.toString(e.getChildren())));
    }

    public static int numberOfLiteralsInRegularCNF(final Node rcnf) {
        Assert.assertTrue(rcnf instanceof And);
        return Arrays.stream(rcnf.getChildren())
                .mapToInt(cs -> cs.getChildren().length)
                .sum();
    }

    public static void removeSemanticDuplicates(final List<Node> formulas) {
        int len = formulas.size();
        for (int i = 0; i < len; ++i) {
            final Node ci = formulas.get(i);

            for (int j = i + 1; j < len; ++j) {
                final Node cj = formulas.get(j);
                if (SAT.equivalent(cj, ci)) {
                    // remove ci
                    // We do this by swapping it with the last element of the list, then reducing the list length by 1
                    // and then continue inspection of the newly swapped in element (thus --i).
                    Collections.swap(formulas, i, len - 1);
                    --i;
                    --len;
                    break;
                }
            }
        }
        formulas.subList(len, formulas.size()).clear();
    }
}
