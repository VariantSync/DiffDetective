import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.serialize.Format;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphExporter;
import org.variantsync.diffdetective.variation.diff.serialize.TikzExporter;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.FullNodeFormat;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class VariationDiffParserTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String testCaseSuffix = ".diff";

    protected static Stream<Path> findTestCases(Path dir) throws IOException {
        return Files
            .list(dir)
            .filter(filename -> filename.getFileName().toString().endsWith(testCaseSuffix));
    }

    public static Stream<Path> tests() throws IOException {
        return findTestCases(testDir);
    }

    public static Stream<Path> wontfixTests() throws IOException {
        return findTestCases(testDir.resolve("wontfix"));
    }

    @ParameterizedTest
    @MethodSource("tests")
    public void test(Path basename) throws IOException, DiffParseException {
        testCase(basename);
    }

    @Disabled("WONTFIX")
    @ParameterizedTest
    @MethodSource("wontfixTests")
    public void wontfixTest(Path testCase) throws IOException, DiffParseException {
        testCase(testCase);
    }

    public void testCase(Path testCasePath) throws IOException, DiffParseException {
        String filename = testCasePath.getFileName().toString();
        String basename = filename.substring(0, filename.length() - testCaseSuffix.length());
        var actualPath = testCasePath.getParent().resolve(basename + "_actual.lg");
        var expectedPath = testCasePath.getParent().resolve(basename + "_expected.lg");

        VariationDiff<DiffLinesLabel> variationDiff;
        try (var inputFile = Files.newBufferedReader(testCasePath)) {
            variationDiff = VariationDiffParser.createVariationDiff(
                inputFile,
                new VariationDiffParseOptions(
                        false,
                        false
                )
            );
        }

        try (var output = IO.newBufferedOutputStream(actualPath)) {
            new LineGraphExporter<>(new Format<>(new FullNodeFormat(), new ChildOrderEdgeFormat<>()))
                .exportVariationDiff(variationDiff, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                // Delete output files if the test succeeded
                Files.delete(actualPath);
            } else {
                // Keep output files if the test failed
                var visualizationPath = testCasePath.getParent().resolve("tex").resolve(basename + ".tex");
                new TikzExporter<>(new Format<>(new FullNodeFormat(), new DefaultEdgeLabelFormat<>()))
                    .exportFullLatexExample(variationDiff, visualizationPath);
                fail("The VariationDiff in file " + testCasePath + " didn't parse correctly. "
                    + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". "
                    + "Note: A visualisation is available at " + visualizationPath);
            }
        }
    }
}
