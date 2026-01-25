import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;

/**
 * Test for {@link VariationDiff#computeAllFeatureNames()}.
 */
public class FeatureNamesTest {
    private final static Path
        diffsDir    = Constants.RESOURCE_DIR.resolve("diffs"),
        pctestDir   = Constants.RESOURCE_DIR.resolve("pctest"),
        ourDir      = Constants.RESOURCE_DIR.resolve("featurenames"),
        collapseDir = diffsDir.resolve("collapse"),
        moveDir     = diffsDir.resolve("move");

    public record TestCase<L extends Label>(String origin, VariationDiff<L> diff, LinkedHashSet<String> features) {
        public static TestCase<DiffLinesLabel> fromFile(Path p, String... features) throws IOException, DiffParseException {
            return new TestCase<>(
                p.toString(),
                VariationDiff.fromFile(p, VariationDiffParseOptions.Default),
                new LinkedHashSet<String>(List.of(features))
            );
        }
    };

    private static List<TestCase<DiffLinesLabel>> diffFeatureNameTestCases() throws IOException, DiffParseException {
        return List.of(
            TestCase.fromFile(collapseDir.resolve("elif.txt"), "A", "B", "C", "D", "E"),
            TestCase.fromFile(collapseDir.resolve("simple.txt"), "A", "B", "C"),
            TestCase.fromFile(moveDir.resolve("simple.txt"), "X", "Y"),
            TestCase.fromFile(pctestDir.resolve("a.diff"), "A", "D", "E", "B", "C"),
            TestCase.fromFile(pctestDir.resolve("elif.diff"), "A", "B", "C", "D"),
            TestCase.fromFile(pctestDir.resolve("else.diff"), "A", "C", "B"),
            TestCase.fromFile(ourDir.resolve("empty.diff")),
            TestCase.fromFile(ourDir.resolve("a.diff"), "FIRST", "SECOND"),
            TestCase.fromFile(ourDir.resolve("b.diff"), "A")
        );
    }

    @ParameterizedTest
    @MethodSource("diffFeatureNameTestCases")
    public void testComputeAllFeatureNames(TestCase<DiffLinesLabel> testCase) {
        assertEquals(testCase.features(), testCase.diff().computeAllFeatureNames(), testCase.origin());
    }
}
