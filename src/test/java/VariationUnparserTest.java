import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
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

    public static Stream<Path> treeTestCases() throws IOException {
        return Files.list(unparserTestCaseDir.resolve("trees"));
    }

    public static Stream<Path> diffTestCases() throws IOException {
        return Stream.concat(
                Files.list(unparserTestCaseDir.resolve("diffs")),
                Files.list(parserTestCaseDir)
                    .filter(filename -> filename.getFileName().toString().endsWith(parserTestCaseSuffix)));
    }

    @ParameterizedTest
    @MethodSource("treeTestCases")
    public void testTreeUnparse(Path testCasePath) throws IOException, DiffParseException {
        String unparsed1 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, false));
        String unparsed2 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, true));
        String unparsed3 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, false));
        String unparsed4 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, true));

        String original = Files.readString(testCasePath);
        original = original.replaceAll("\\r\\n", "\n");

        assertEqualTree(original, unparsed1);
        assertEqualTree(original, unparsed2);
        assertEqualTree(original, unparsed3);
        assertEqualTree(original, unparsed4);
    }

    @ParameterizedTest
    @MethodSource("diffTestCases")
    public void testDiffUnparse(Path testCasePath) throws IOException, DiffParseException {
        String unparsed1 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, false));
        String unparsed2 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, true));
        String unparsed3 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, false));
        String unparsed4 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, true));

        String original = Files.readString(testCasePath);
        original = original.replaceAll("\\r\\n", "\n");

        assertEqualDiff(original, unparsed1);
        assertEqualDiff(original, unparsed2);
        assertEqualDiff(original, unparsed3);
        assertEqualDiff(original, unparsed4);
    }

    private static String parseUnparseTree(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path, option);
        return VariationUnparser.variationTreeUnparser(tree);
    }

    private static String parseUnparseDiff(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> diff = VariationDiff.fromFile(path, option);
        return VariationUnparser.variationDiffUnparser(diff);
    }

    private static void assertEqualDiff(String expected, String actual) {
        assertEqualTree(VariationUnparser.undiff(expected, Time.BEFORE), VariationUnparser.undiff(actual, Time.BEFORE));
        assertEqualTree(VariationUnparser.undiff(expected, Time.AFTER), VariationUnparser.undiff(actual, Time.AFTER));
    }

    private static void assertEqualTree(String expected, String actual) {
        assertEquals(removeWhitespace(expected, false), removeWhitespace(actual, false));
    }
}
