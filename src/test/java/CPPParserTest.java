import org.junit.Assert;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.feature.CPPDiffLineFormulaExtractor;

import java.util.List;

public class CPPParserTest {
    private record TestCase(
            String diffLine,
            String expectedExtractionResult,
            Node expectedFormula
    ) {}

    private static TestCase tc(
            String diffLine,
            String expectedExtractionResult,
            Node expectedFormula) {
        return new TestCase(diffLine, expectedExtractionResult, expectedFormula);
    }

    private static final Literal A = new Literal("A");
    private static final Literal B = new Literal("B");

    private static final List<TestCase> EXTRACTOR_TEST_CASES = List.of(
            tc(
                    "#if x + 3 > 1",
                    "x__ADD__3__GT__1",
                    new Literal("x__ADD__3__GT__1")
            ),
            tc(
                    "#if A && B",
                    "A&&B",
                    new And(A, B)
            ),
            tc(
                    // BUG: This is not correctly parsed because of brackets.
                    "#if defined(A) && (B * 2) > C",
                    "A&&__LB__B__MUL__2__RB____GT__C",
                    new And(A, new Literal("__LB__B__MUL__2__RB____GT__C"))
            )
    );

    @Test
    public void testFormulaExtractor() throws IllFormedAnnotationException {
        final CPPDiffLineFormulaExtractor extractor = new CPPDiffLineFormulaExtractor();
        for (final var testcase : EXTRACTOR_TEST_CASES) {
            Assert.assertEquals(
                    testcase.expectedExtractionResult(),
                    extractor.extractFormula(testcase.diffLine())
            );
        }
    }

    @Test
    public void testFormulaParser() throws IllFormedAnnotationException {
        final CPPAnnotationParser parser = CPPAnnotationParser.Default;
        for (final var testcase : EXTRACTOR_TEST_CASES) {
            final Node parsedExtractionResult = parser.parseCondition(testcase.expectedExtractionResult());
            final Node parsedDiffline = parser.parseDiffLine(testcase.diffLine());
            final Node expectedFormula = testcase.expectedFormula();
            Assert.assertEquals(
                    parsedExtractionResult,
                    parsedDiffline
            );
            Assert.assertEquals(
                    parsedDiffline,
                    expectedFormula
            );
        }
    }
}
