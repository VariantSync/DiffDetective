import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.FileSource;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.util.Source;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.GumTreeDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;
import org.variantsync.diffdetective.variation.tree.VariationTree;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

public class TreeDiffingTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("tree-diffing");
    private static Pattern expectedFileNameRegex = Pattern.compile("([^_]+)_([^_]+)_expected.lg");

    private static record TestCase(String expectedDir, String basename, String matcherName, Matcher matcher) {
        public Path beforeEdit() {
            return testDir.resolve(String.format("%s.before", basename()));
        }

        public Path afterEdit() {
            return testDir.resolve(String.format("%s.after", basename()));
        }

        public Path actual() {
            return testDir.resolve(expectedDir).resolve(String.format("%s_%s_actual.lg", basename(), matcherName()));
        }

        public Path expected() {
            return testDir.resolve(expectedDir).resolve(String.format("%s_%s_expected.lg", basename(), matcherName()));
        }

        public Path visualisation() {
            return testDir.resolve(expectedDir).resolve("tex").resolve(String.format("%s_%s.tex", basename(), matcherName()));
        }
    }

    private static Stream<TestCase> testCases(String expectedDir) throws IOException {
        return Files
                .list(testDir.resolve(expectedDir))
                .mapMulti(((path, result) -> {
                    String filename = path.getFileName().toString();
                    var filenameMatcher = expectedFileNameRegex.matcher(filename);
                    if (filenameMatcher.matches()) {
                        var treeMatcherName = filenameMatcher.group(2);

                        result.accept(new TestCase(
                                expectedDir,
                                filenameMatcher.group(1),
                                treeMatcherName,
                                Matchers.getInstance().getMatcher(treeMatcherName))
                        );
                    }
                }));
    }

    private static Stream<TestCase> createMatchingTestCases() throws IOException {
        return testCases("createMatching");
    }

    @ParameterizedTest
    @MethodSource("createMatchingTestCases")
    public void createMatchingTestCase(TestCase testCase) throws IOException, DiffParseException {
        VariationTree<DiffLinesLabel> beforeEdit = VariationTree.fromFile(testCase.beforeEdit());
        VariationTree<DiffLinesLabel> afterEdit = VariationTree.fromFile(testCase.afterEdit());
        assertExpectedVariationDiffs(testCase, GumTreeDiff.diffUsingMatching(beforeEdit, afterEdit, testCase.matcher()));
    }

    private static Stream<TestCase> improveMatchingTestCases() throws IOException {
        return testCases("improveMatching");
    }

    @ParameterizedTest
    @MethodSource("improveMatchingTestCases")
    public void improveMatchingTestCase(TestCase testCase) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> variationDiff =
            VariationDiff.fromFiles(
                testCase.beforeEdit(),
                testCase.afterEdit(),
                DiffAlgorithm.SupportedAlgorithm.MYERS,
                VariationDiffParseOptions.Default
            );

        DiffNode<DiffLinesLabel> improvedDiffNode = GumTreeDiff.improveMatching(variationDiff.getRoot(), testCase.matcher());
        VariationDiff<DiffLinesLabel> improvedVariationDiff = new VariationDiff<>(improvedDiffNode, Source.Unknown);

        assertExpectedVariationDiffs(testCase, improvedVariationDiff);
    }

    private static void assertExpectedVariationDiffs(TestCase testCase, VariationDiff<DiffLinesLabel> variationDiff) throws IOException {
        try (var output = IO.newBufferedOutputStream(testCase.actual())) {
            new LineGraphExporter<>(new Format<>(new FullNodeFormat(), new ChildOrderEdgeFormat<>()))
                    .exportVariationDiff(variationDiff, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(testCase.expected());
                var actualFile = Files.newBufferedReader(testCase.actual());
        ) {
            if (IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                // Delete output files if the test succeeded
                Files.delete(testCase.actual());
            } else {
                // Keep output files if the test failed
                new TikzExporter<>(new Format<>(new FullNodeFormat(), new DefaultEdgeLabelFormat<>()))
                        .exportFullLatexExample(variationDiff, testCase.visualisation());
                fail(String.format(
                        "The diff of %s and %s is not as expected. " +
                                "Expected the content of %s but got the content of %s. " +
                                "Note: A visualisation is available at %s",
                        testCase.beforeEdit(),
                        testCase.afterEdit(),
                        testCase.expected(),
                        testCase.actual(),
                        testCase.visualisation()
                ));
            }
        }
    }
}
