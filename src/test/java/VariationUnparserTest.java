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
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.variantsync.diffdetective.experiments.thesis_es.UnparseAnalysis.removeWhitespace;

public class VariationUnparserTest {
    private final static Path testDirTree = Constants.RESOURCE_DIR.resolve("unparser");

    private final static Path testDirDiff = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String testCaseSuffixTree = ".txt";

    private final static String testCaseSuffixDiff = ".diff";

    protected static Stream<Path> findTestCases(Path dir, String testCaseSuffix) throws IOException {
        return Files
                .list(dir)
                .filter(filename -> filename.getFileName().toString().endsWith(testCaseSuffix));
    }

    public static Stream<Path> testsTree() throws IOException {
        return findTestCases(testDirTree, testCaseSuffixTree);
    }

    public static Stream<Path> testsDiff() throws IOException {
        return findTestCases(testDirDiff, testCaseSuffixDiff);
    }

    @Test
    public void testTree() throws IOException, DiffParseException {
        String source = Files.readString(testDirTree.resolve("test8.txt"));
        VariationTree<DiffLinesLabel> tree = VariationTree.fromText(source, VariationTreeSource.Unknown,
                VariationDiffParseOptions.Default);
        String temp = VariationUnparser.variationTreeUnparser(tree);

        assertEquals(removeWhitespace(source, false), removeWhitespace(temp, false));
    }

    @Test
    public void testDiffSemEq() throws IOException, DiffParseException {
        String source = Files
                .readString(testDirTree.resolve("diff").resolve("diff.diff"));
        VariationDiff<DiffLinesLabel> diff = VariationDiff.fromDiff(source, VariationDiffParseOptions.Default);
        String temp = VariationUnparser.variationDiffUnparser(diff);

        assertEquals(
                removeWhitespace(VariationUnparser.undiff(source, Time.BEFORE), false),
                removeWhitespace(VariationUnparser.undiff(temp, Time.BEFORE), false));
        assertEquals(
                removeWhitespace(VariationUnparser.undiff(source, Time.AFTER), false),
                removeWhitespace(VariationUnparser.undiff(temp, Time.AFTER), false));
    }

    @ParameterizedTest
    @MethodSource("testsTree")
    public void testCaseTree(Path testCasePath) throws IOException, DiffParseException {
        String temp = Files.readString(testCasePath);
        temp = temp.replaceAll("\\r\\n", "\n");

        String unparse1 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, false));
        String unparse2 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, true));
        String unparse3 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, false));
        String unparse4 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, true));

        temp = removeWhitespace(temp, false);
        unparse1 = removeWhitespace(unparse1, false);
        unparse2 = removeWhitespace(unparse2, false);
        unparse3 = removeWhitespace(unparse3, false);
        unparse4 = removeWhitespace(unparse4, false);

        assertEquals(temp, unparse1);
        assertEquals(temp, unparse2);
        assertEquals(temp, unparse3);
        assertEquals(temp, unparse4);
    }

    @ParameterizedTest
    @MethodSource("testsDiff")
    public void testCaseDiff(Path testCasePath) throws IOException, DiffParseException {
        String temp = Files.readString(testCasePath);
        temp = temp.replaceAll("\\r\\n", "\n");

        String unparse1 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, false));
        String unparse2 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, true));
        String unparse3 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, false));
        String unparse4 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, true));

        String temp1 = VariationUnparser.undiff(temp, Time.BEFORE);
        String temp2 = VariationUnparser.undiff(temp, Time.AFTER);
        String unparse11 = VariationUnparser.undiff(unparse1, Time.BEFORE);
        String unparse12 = VariationUnparser.undiff(unparse1, Time.AFTER);
        String unparse21 = VariationUnparser.undiff(unparse2, Time.BEFORE);
        String unparse22 = VariationUnparser.undiff(unparse2, Time.AFTER);
        String unparse31 = VariationUnparser.undiff(unparse3, Time.BEFORE);
        String unparse32 = VariationUnparser.undiff(unparse3, Time.AFTER);
        String unparse41 = VariationUnparser.undiff(unparse4, Time.BEFORE);
        String unparse42 = VariationUnparser.undiff(unparse4, Time.AFTER);

        assertEquals(removeWhitespace(temp1, false), removeWhitespace(unparse11, false));
        assertEquals(removeWhitespace(temp2, false), removeWhitespace(unparse12, false));
        assertEquals(removeWhitespace(temp1, false), removeWhitespace(unparse21, false));
        assertEquals(removeWhitespace(temp2, false), removeWhitespace(unparse22, false));
        assertEquals(removeWhitespace(temp1, false), removeWhitespace(unparse31, false));
        assertEquals(removeWhitespace(temp2, false), removeWhitespace(unparse32, false));
        assertEquals(removeWhitespace(temp1, false), removeWhitespace(unparse41, false));
        assertEquals(removeWhitespace(temp2, false), removeWhitespace(unparse42, false));
    }

    public static String parseUnparseTree(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path, option);
        return VariationUnparser.variationTreeUnparser(tree);
    }

    public static String parseUnparseDiff(Path path, VariationDiffParseOptions option) throws IOException, DiffParseException {
        VariationDiff<DiffLinesLabel> diff = VariationDiff.fromFile(path, option);
        return VariationUnparser.variationDiffUnparser(diff);
    }
}
