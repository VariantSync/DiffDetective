package analysis;

import org.prop4j.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * @author Chico Sundermann, Paul Bittner
 */
public class Tseytin {
    private static class Convert {
        private final List<Node> newSubFormulas;
        private int currentIndex = 0;
        private final List<String> helperVariables;

        private final BiFunction<Node, Node, Node> eq;

        private Convert(final Node formula, final BiFunction<Node, Node, Node> eq) {
            this.eq = eq;
            formula.simplifyTree();
            helperVariables = new ArrayList<>();
            newSubFormulas = new ArrayList<>();
            newSubFormulas.add(tseytin(formula));
        }

        private Node tseytin(Node formula) {
            if (formula instanceof Literal) {
                return formula;
            } else {
                List<Node> newChildren = new ArrayList<>();
                for (Node child : formula.getChildren()) {
                    newChildren.add(tseytin(child));
                }
                String helperVariable = getNextVariableName();
                Literal tseitinVar = new Literal(helperVariable, true);
                helperVariables.add(helperVariable);
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
            return "t" + currentIndex++;
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
