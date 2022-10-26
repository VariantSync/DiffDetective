import org.junit.Assert;
import org.junit.Test;
import org.prop4j.And;
import org.prop4j.Literal;
import org.prop4j.Node;
import org.variantsync.diffdetective.datasets.DatasetDescription;
import org.variantsync.diffdetective.datasets.DatasetFactory;
import org.variantsync.diffdetective.diff.GitPatch;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.parse.IllFormedAnnotationException;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.feature.CPPDiffLineFormulaExtractor;
import org.variantsync.functjonal.Result;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CPPParserTest {
    private record TestCase(
            String diffLine,
            String expectedExtractionResult,
            Node expectedFormula
    ) {}

    private static final Path reposPathWSL = Paths.get("\\\\wsl$", "Ubuntu/home/bittner/VariantSync", "DiffDetectiveMining");

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

    @Test
    public void testLibSSHx() throws IOException {
        final Result<DiffTree, List<DiffError>> dr = DiffTree.fromPatch(
                new GitPatch.PatchReference(
                "tests/pkd/pkd_hello.c",
                "c8f49becfde6777aa73cea3c8aa58a752d2adce4",
                "f64814b7be533080a7117cd174c3a81d859f4399"
                ),
                new DatasetFactory(reposPathWSL).create(DatasetDescription.summary(
                        "libssh",
                        "https://gitlab.com/libssh/libssh-mirror"
                ))
        );
        
        Assert.assertTrue(dr.isFailure());
    }

    @Test
    public void testOpenSolaris() throws IOException {
        final Result<DiffTree, List<DiffError>> dr = DiffTree.fromPatch(
                new GitPatch.PatchReference(
                        "usr/src/uts/common/zmod/zutil.h",
                        "65b50c042ab274a35562ae78f4405e5c0bda1ba3",
                        "56b2069abbf58be3bfc16fdadeb12b26063b130f"
                ),
                new DatasetFactory(reposPathWSL).create(DatasetDescription.summary(
                        "opensolaris",
                        "https://github.com/kofemann/opensolaris"
                ))
        );

        Assert.assertTrue(dr.isFailure());
    }
}
