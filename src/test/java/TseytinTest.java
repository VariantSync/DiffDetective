import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.prop4j.*;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.analysis.logic.Tseytin;
import org.variantsync.diffdetective.util.fide.FixTrueFalse;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class TseytinTest {
    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");
    private static final Literal C = new Literal("C");
    private static final Literal D = new Literal("D");

    private static List<Node> tautologyTestCases;
    private static List<Node> contradictoryTestCases;
    private static List<Node> satisfiableTestCases;

    @BeforeAll
    public static void setupTestCases() {
        tautologyTestCases = List.of(
                FixTrueFalse.True,
                negate(FixTrueFalse.False),
                new Or(A, negate(A)),
                new Implies(new And(A, new Implies(A, B)), B) // modus ponens
        );

        contradictoryTestCases = List.of(
                FixTrueFalse.False,
                negate(FixTrueFalse.True),
                new And(A, negate(A))
        );

        satisfiableTestCases = new ArrayList<>(List.of(
                A,
                negate(A),
                negate(new And(A, B)),
                new And(A, B),
                new Or(A, B),
                new Implies(A, B),
                new Equals(A, B)
        ));
        satisfiableTestCases.addAll(tautologyTestCases);
    }

    @Test
    public void testSAT() {
        for (final Node formula : satisfiableTestCases) {
            assertTrue(SAT.isSatisfiableNoTseytin(formula), formula.toString());
            assertTrue(SAT.isSatisfiableAlwaysTseytin(formula), formula.toString());
        }
    }

    @Test
    public void testTAUT() {
        for (final Node formula : tautologyTestCases) {
            final Node no = negate(formula);
            assertFalse(
                    SAT.isSatisfiableNoTseytin(no),
                    no.toString()
            );
            assertFalse(
                    SAT.isSatisfiableAlwaysTseytin(no),
                    "Expected SAT(tseytin(" + no + ")) = SAT(" + Tseytin.toEquisatisfiableCNF(no) + ") = false but got true."
            );
        }
    }

    @Test
    public void testContradictions() {
        for (final Node formula : contradictoryTestCases) {
            assertFalse(SAT.isSatisfiableNoTseytin(formula), formula.toString());
            assertFalse(SAT.isSatisfiableAlwaysTseytin(formula), formula.toString());
        }
    }
}
