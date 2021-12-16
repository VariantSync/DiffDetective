import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.prop4j.*;

import java.util.List;

import static util.fide.FixTrueFalse.*;
import static util.fide.FormulaUtils.negate;

public class FixTrueFalseTest {
    private record TestCase(Node formula, Node expectedResult) {}

    private List<TestCase> testCases;

    private final static Literal A = new Literal("A");
    private final static Literal B = new Literal("B");
    private final static Literal C = new Literal("C");
    private final static Node SomeIrreducible = new And(A, new Implies(A, B));

    @Before
    public void initTestCases() {
        testCases = List.of(
                new TestCase(new And(True, A), A),
                new TestCase(new Or(False, A), A),
                new TestCase(new And(False, A), False),
                new TestCase(new Or(True, A), True),

                new TestCase(new Implies(False, A), True),
                new TestCase(new Implies(A, False), negate(A)),
                new TestCase(new Implies(True, A), A),
                new TestCase(new Implies(A, True), True),

                new TestCase(new Equals(A, True), A),
                new TestCase(new Equals(True, A), A),
                new TestCase(new Equals(A, False), negate(A)),
                new TestCase(new Equals(False, A), negate(A)),
                
                new TestCase(
                        new Equals(
                                new Or(
                                        new And(False, True, A),
                                        SomeIrreducible
                                ),
                                new Implies(
                                        new Or(False, C),
                                        new Not(False)
                                )
                        ),
                        SomeIrreducible)
        );
    }

    @Test
    public void testAll() {
        for (TestCase testCase : testCases) {
            Assert.assertEquals(On(testCase.formula), testCase.expectedResult);
        }
    }
}
