package org.variantsync.diffdetective.analysis.logic;

import org.prop4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * Class with methods for Tseytin conversion.
 * The Tseytin conversion produces an equisatisfiable CNF for a propositional formula.
 * The produced CNF is usually small at the cost of introducing new variables.
 *
 * @author Chico Sundermann, Paul Bittner
 */
public final class Tseytin {
    private Tseytin() {}

    /**
     * Helper class for Tseytin conversion that remembers generated formulas.
     * This helper class basically models the conversion function equipped with some useful state.
     * (Function programmers might see this as a function within a state monad.)
     */
    private static class Convert {
        private final List<Node> newSubFormulas;
        private int currentIndex = 0;
//        private final List<String> helperVariables;

        private final BiFunction<Node, Node, Node> eq;

        /**
         * Convertes the given formula with the given equivalence function.
         * @param formula Formula to tseytin convert.
         * @param eq Function that models the equivelency relationship between two given nodes. Typically, this
         *           function produces a propositional "iff" (&lt;=&gt;).
         */
        private Convert(final Node formula, final BiFunction<Node, Node, Node> eq) {
            this.eq = eq;
            formula.simplifyTree();
//            helperVariables = new ArrayList<>();
            newSubFormulas = new ArrayList<>();
            newSubFormulas.add(tseytin(formula
//                    , true
            ));
        }

        /**
         * Performs tseyting conversion on the given formula.
         * @param formula Formula to convert.
         * @return The tseyting variable representing the input formula.
         */
        private Node tseytin(Node formula
//                , boolean isRoot
        ) {
            if (formula instanceof Literal) {
                return formula;
            } else {
                List<Node> newChildren = new ArrayList<>();
                for (Node child : formula.getChildren()) {
                    newChildren.add(tseytin(child
//                            , false
                    ));
                }

//                BiFunction<Node, Node, Node> eq;
//                if (isRoot) {
//                    eq = Equals::new;
//                } else {
//                    eq = Implies::new;
//                }

                String helperVariable = getNextVariableName();
                Literal tseitinVar = new Literal(helperVariable, true);
//                helperVariables.add(helperVariable);
                if (formula instanceof And) {
                    newSubFormulas.add(eq.apply(tseitinVar, new And(newChildren)).toCNF());
                } else if (formula instanceof Or) {
                    newSubFormulas.add(eq.apply(tseitinVar, new Or(newChildren)).toCNF());
                } else if (formula instanceof Equals) {
                    newSubFormulas.add(eq.apply(tseitinVar, new Equals(newChildren.get(0), newChildren.get(1))).toCNF());
                } else if (formula instanceof Implies) {
                    newSubFormulas.add(eq.apply(tseitinVar, new Implies(newChildren.get(0), newChildren.get(1))).toCNF());
                } else if (formula instanceof Not) {
                    newSubFormulas.add(eq.apply(tseitinVar, new Not(newChildren.get(0))).toCNF());
                }
                return tseitinVar;
            }
        }

        private String getNextVariableName() {
            return "$t" + currentIndex++ + "$";
        }
    }

    private static Node convert(final Node formula, final BiFunction<Node, Node, Node> eq) {
        return new And(new Convert(formula, eq).newSubFormulas);
    }

    public static Node toEquisatisfiableCNF(final Node formula) {
        return convert(formula, Implies::new);
    }

    public static Node toEquivalentCNF(final Node formula) {
        return convert(formula, Equals::new);
    }
}
