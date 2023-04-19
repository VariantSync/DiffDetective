import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.prop4j.*;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.analysis.logic.Tseytin;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class SATTest {
    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");

    public static List<Node> tautologyTestCases() {
        return List.of(
                FixTrueFalse.True,
                negate(FixTrueFalse.False),
                new Or(A, negate(A)),
                new Implies(new And(A, new Implies(A, B)), B) // modus ponens
        );
    }

    public static List<Node> contradictoryTestCases() {
        return List.of(
                FixTrueFalse.False,
                negate(FixTrueFalse.True),
                new And(A, negate(A))
        );
    }

    public static List<Node> satisfiableTestCases() {
       var satisfiableTestCases = new ArrayList<>(List.of(
                A,
                negate(A),
                negate(new And(A, B)),
                new And(A, B),
                new Or(A, B),
                new Implies(A, B),
                new Equals(A, B)
        ));
        satisfiableTestCases.addAll(tautologyTestCases());
        return satisfiableTestCases;
    }


    public static boolean isSatisfiableNoTseytin(final Node formula) {
        return SAT.checkSATviaSat4J(FixTrueFalse.EliminateTrueAndFalse(formula));
    }

    public static boolean isSatisfiableAlwaysTseytin(final Node formula) {
        return SAT.checkSATviaSat4J(FixTrueFalse.EliminateTrueAndFalse(Tseytin.toEquivalentCNF(formula)));
    }

    public static boolean isSatisfiableDNF(final Node formula) {
        return SAT.checkSATviaDNF(FixTrueFalse.EliminateTrueAndFalse(formula));
    }

    @ParameterizedTest
    @MethodSource("satisfiableTestCases")
    public void testSAT(Node formula) {
        assertTrue(SAT.isSatisfiable(formula), formula.toString());
        assertTrue(isSatisfiableDNF(formula), formula.toString());
        assertTrue(isSatisfiableNoTseytin(formula), formula.toString());
        assertTrue(isSatisfiableAlwaysTseytin(formula), formula.toString());
    }

    @ParameterizedTest
    @MethodSource("tautologyTestCases")
    public void testTAUT(Node formula) {
        final Node no = negate(formula);
        assertFalse(
                SAT.isSatisfiable(no),
                no.toString()
        );
        assertFalse(
                isSatisfiableDNF(no),
                no.toString()
        );
        assertFalse(
                isSatisfiableNoTseytin(no),
                no.toString()
        );
        assertFalse(
                isSatisfiableAlwaysTseytin(no),
                "Expected SAT(tseytin(" + no + ")) = SAT(" + Tseytin.toEquisatisfiableCNF(no) + ") = false but got true."
        );
    }

    @ParameterizedTest
    @MethodSource("contradictoryTestCases")
    public void testContradictions(Node formula) {
        assertFalse(SAT.isSatisfiable(formula), formula.toString());
        assertFalse(isSatisfiableDNF(formula), formula.toString());
        assertFalse(isSatisfiableNoTseytin(formula), formula.toString());
        assertFalse(isSatisfiableAlwaysTseytin(formula), formula.toString());
    }
}
