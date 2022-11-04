import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.Format;
import org.variantsync.diffdetective.diff.difftree.serialize.TikzExporter;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.ChildOrderEdgeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.FullNodeFormat;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class DiffTreeParserTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String testCaseSuffix = ".diff";

    private static Stream<Path> findTestCases(Path dir) throws IOException {
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
        var actualPath = testDir.resolve(basename + "_actual.lg");
        var expectedPath = testDir.resolve(basename + "_expected.lg");

        DiffTree diffTree;
        try (var inputFile = Files.newBufferedReader(testCasePath)) {
            diffTree = DiffTreeParser.createDiffTree(
                inputFile,
                false,
                false,
                CPPAnnotationParser.Default
            );
        }

        try (
                var unbufferedOutput = Files.newOutputStream(actualPath);
                var output = new BufferedOutputStream(unbufferedOutput)
        ) {
            new LineGraphExporter(new Format(new FullNodeFormat(), new ChildOrderEdgeFormat()))
                .exportDiffTree(diffTree, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                var visualizationPath = testDir.resolve(basename + ".tex");
                new TikzExporter(new Format(new FullNodeFormat(), new DefaultEdgeLabelFormat()))
                    .exportFullLatexExample(diffTree, visualizationPath);
                fail("The DiffTree in file " + testCasePath + " didn't parse correctly. "
                    + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". "
                    + "Note: A visualisation is available at " + visualizationPath);
                // Keep output files if the test failed
            } else {
                // Delete output files if the test succeeded
                Files.delete(actualPath);
            }
        }
    }
}
