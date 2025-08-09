import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
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
    private final static Path treeDir = Constants.RESOURCE_DIR.resolve("unparser");
    private final static Path testDirDiff = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String treeSuffix = ".txt";
    private final static String diffSuffix = ".diff";

    private static Stream<Path> findTestCases(Path dir, String filenameSuffix) throws IOException {
        return Files
                .list(dir)
                .filter(filename -> filename.getFileName().toString().endsWith(filenameSuffix));
    }

    public static Stream<Path> testsTree() throws IOException {
        return findTestCases(treeDir, treeSuffix);
    }

    public static Stream<Path> testsDiff() throws IOException {
        return findTestCases(testDirDiff, diffSuffix);
    }

    @Test
    public void testTree() throws IOException, DiffParseException {
        Path file = treeDir.resolve("test8.txt");

        assertEqualTree(
            Files.readString(file),
            parseUnparseTree(file, VariationDiffParseOptions.Default));
    }

    @Test
    public void testDiffSemEq() throws IOException, DiffParseException {
        Path file = treeDir.resolve("diff").resolve("diff.diff");

        assertEqualDiff(
            Files.readString(file),
            parseUnparseDiff(file, VariationDiffParseOptions.Default));
    }

    @ParameterizedTest
    @MethodSource("testsTree")
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
    @MethodSource("testsDiff")
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
