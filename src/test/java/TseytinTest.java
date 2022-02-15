import analysis.SAT;
import analysis.Tseytin;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.*;
import util.fide.FixTrueFalse;

import java.util.ArrayList;
import java.util.List;

import static util.fide.FormulaUtils.negate;

public class TseytinTest {
    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");
    private static final Literal C = new Literal("C");
    private static final Literal D = new Literal("D");

    private static List<Node> tautologyTestCases;
    private static List<Node> contradictoryTestCases;
    private static List<Node> satisfiableTestCases;

    @BeforeClass
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
            Assert.assertTrue(formula.toString(), SAT.isSatisfiableNoTseytin(formula));
            Assert.assertTrue(formula.toString(), SAT.isSatisfiableAlwaysTseytin(formula));
        }
    }

    @Test
    public void testTAUT() {
        for (final Node formula : tautologyTestCases) {
            final Node no = negate(formula);
            Assert.assertFalse(
                    no.toString(),
                    SAT.isSatisfiableNoTseytin(no)
            );
            Assert.assertFalse(
                    "Expected SAT(tseytin(" + no + ")) = SAT(" + Tseytin.toEquisatisfiableCNF(no) + ") = false but got true.",
                    SAT.isSatisfiableAlwaysTseytin(no)
            );
        }
    }

    @Test
    public void testContradictions() {
        for (final Node formula : contradictoryTestCases) {
            Assert.assertFalse(formula.toString(), SAT.isSatisfiableNoTseytin(formula));
            Assert.assertFalse(formula.toString(), SAT.isSatisfiableAlwaysTseytin(formula));
        }
    }
}
