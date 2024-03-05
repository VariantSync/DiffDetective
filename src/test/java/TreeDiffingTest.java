import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.PreprocessorAnnotationParser;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.construction.GumTreeDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.tree.source.LocalFileSource;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

public class TreeDiffingTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("tree-diffing");
    private static Pattern expectedFileNameRegex = Pattern.compile("([^_]+)_([^_]+)_expected.lg");

    private static record TestCase(String basename, String matcherName, Matcher matcher) {
        public Path beforeEdit() {
            return testDir.resolve(String.format("%s.before", basename()));
        }

        public Path afterEdit() {
            return testDir.resolve(String.format("%s.after", basename()));
        }

        public Path actual() {
            return testDir.resolve(String.format("%s_%s_actual.lg", basename(), matcherName()));
        }

        public Path expected() {
            return testDir.resolve(String.format("%s_%s_expected.lg", basename(), matcherName()));
        }

        public Path visualisation() {
            return testDir.resolve("tex").resolve(String.format("%s_%s.tex", basename(), matcherName()));
        }
    }

    private static Stream<TestCase> testCases() throws IOException {
        return Files
                .list(testDir)
                .mapMulti(((path, result) -> {
                    String filename = path.getFileName().toString();
                    var filenameMatcher = expectedFileNameRegex.matcher(filename);
                    if (filenameMatcher.matches()) {
                        var treeMatcherName = filenameMatcher.group(2);

                        result.accept(new TestCase(
                                filenameMatcher.group(1),
                                treeMatcherName,
                                Matchers.getInstance().getMatcher(treeMatcherName))
                        );
                    }
                }));
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testCase(TestCase testCase) throws IOException, DiffParseException {
        VariationTree<DiffLinesLabel> beforeEdit = parseVariationTree(testCase.beforeEdit());
        VariationTree<DiffLinesLabel> afterEdit = parseVariationTree(testCase.afterEdit());

        VariationDiff<DiffLinesLabel> variationDiff = GumTreeDiff.diffUsingMatching(beforeEdit, afterEdit);

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

    public VariationTree<DiffLinesLabel> parseVariationTree(Path filename) throws IOException, DiffParseException {
        try (var file = Files.newBufferedReader(filename)) {
            return new VariationTree<>(
                    VariationDiffParser.createVariationTree(
                            file,
                            new VariationDiffParseOptions(
                                    PreprocessorAnnotationParser.CPPAnnotationParser,
                                    false,
                                    false)
                    ).getRoot().projection(BEFORE).toVariationTree(),
                    new LocalFileSource(filename)
            );
        }
    }
}
