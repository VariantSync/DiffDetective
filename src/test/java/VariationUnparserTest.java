import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.VariationUnparser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.variantsync.diffdetective.experiments.thesis_es.UnparseAnalysis.removeWhitespace;

public class VariationUnparserTest {
    private final static Path unparserTestCaseDir = Constants.RESOURCE_DIR.resolve("unparser");
    private final static Path parserTestCaseDir = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String parserTestCaseSuffix = ".diff";

    public static Stream<Arguments> treeTestCases() throws IOException {
        return withParseOptions(Files.list(unparserTestCaseDir.resolve("trees")));
    }

    public static Stream<Arguments> diffTestCases() throws IOException {
        return withParseOptions(Stream.concat(
                Files.list(unparserTestCaseDir.resolve("diffs")),
                Files.list(parserTestCaseDir)
                    .filter(filename -> filename.getFileName().toString().endsWith(parserTestCaseSuffix))));
    }

    private static Stream<Arguments> withParseOptions(Stream<Path> paths) {
        // Build a Cartesian product of all paths and parse options.
        return
            paths.flatMap(path -> Stream.of(
                Arguments.of(path, new VariationDiffParseOptions(false, false)),
                Arguments.of(path, new VariationDiffParseOptions(false, true)),
                Arguments.of(path, new VariationDiffParseOptions(true, false)),
                Arguments.of(path, new VariationDiffParseOptions(true, true))));
    }

    @ParameterizedTest
    @MethodSource("treeTestCases")
    public void testTreeUnparse(Path testCasePath, VariationDiffParseOptions parseOptions) throws IOException, DiffParseException {
        assertEqualTree(
            Files.readString(testCasePath).replaceAll("\\r\\n", "\n"),
            parseUnparseTree(testCasePath, parseOptions));
    }

    @ParameterizedTest
    @MethodSource("diffTestCases")
    public void testDiffUnparse(Path testCasePath, VariationDiffParseOptions parseOptions) throws IOException, DiffParseException {
        assertEqualDiff(
            Files.readString(testCasePath).replaceAll("\\r\\n", "\n"),
            parseUnparseDiff(testCasePath, parseOptions));
    }

    private static String parseUnparseTree(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path, option);
        return VariationUnparser.unparseTree(tree);
    }

    private static String parseUnparseDiff(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> diff = VariationDiff.fromFile(path, option);
        return VariationUnparser.unparseDiff(diff);
    }

    private static void assertEqualDiff(String expected, String actual) {
        assertEqualTree(VariationUnparser.undiff(expected, Time.BEFORE), VariationUnparser.undiff(actual, Time.BEFORE));
        assertEqualTree(VariationUnparser.undiff(expected, Time.AFTER), VariationUnparser.undiff(actual, Time.AFTER));
    }

    private static void assertEqualTree(String expected, String actual) {
        assertEquals(removeWhitespace(expected, false), removeWhitespace(actual, false));
    }
}
