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
    private final static Path treeDir = Constants.RESOURCE_DIR.resolve("unparser");
    private final static Path testDirDiff = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String treeSuffix = ".txt";
    private final static String diffSuffix = ".diff";

    protected static Stream<Path> findTestCases(Path dir, String filenameSuffix) throws IOException {
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
        String original = Files.readString(treeDir.resolve("test8.txt"));
        VariationTree<DiffLinesLabel> tree =
            VariationTree.fromText(original, VariationTreeSource.Unknown, VariationDiffParseOptions.Default);
        String unparsed = VariationUnparser.variationTreeUnparser(tree);

        assertEquals(removeWhitespace(original, false), removeWhitespace(unparsed, false));
    }

    @Test
    public void testDiffSemEq() throws IOException, DiffParseException {
        String original = Files.readString(treeDir.resolve("diff").resolve("diff.diff"));
        VariationDiff<DiffLinesLabel> diff = VariationDiff.fromDiff(original, VariationDiffParseOptions.Default);
        String unparsed = VariationUnparser.variationDiffUnparser(diff);

        assertEquals(
                removeWhitespace(VariationUnparser.undiff(original, Time.BEFORE), false),
                removeWhitespace(VariationUnparser.undiff(unparsed, Time.BEFORE), false));
        assertEquals(
                removeWhitespace(VariationUnparser.undiff(original, Time.AFTER), false),
                removeWhitespace(VariationUnparser.undiff(unparsed, Time.AFTER), false));
    }

    @ParameterizedTest
    @MethodSource("testsTree")
    public void testTreeUnparse(Path testCasePath) throws IOException, DiffParseException {
        String original = Files.readString(testCasePath);
        original = original.replaceAll("\\r\\n", "\n");

        String unparsed1 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, false));
        String unparsed2 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, true));
        String unparsed3 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, false));
        String unparsed4 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, true));

        original = removeWhitespace(original, false);
        unparsed1 = removeWhitespace(unparsed1, false);
        unparsed2 = removeWhitespace(unparsed2, false);
        unparsed3 = removeWhitespace(unparsed3, false);
        unparsed4 = removeWhitespace(unparsed4, false);

        assertEquals(original, unparsed1);
        assertEquals(original, unparsed2);
        assertEquals(original, unparsed3);
        assertEquals(original, unparsed4);
    }

    @ParameterizedTest
    @MethodSource("testsDiff")
    public void testDiffUnparse(Path testCasePath) throws IOException, DiffParseException {
        String original = Files.readString(testCasePath);
        original = original.replaceAll("\\r\\n", "\n");

        String unparsed1 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, false));
        String unparsed2 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, true));
        String unparsed3 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, false));
        String unparsed4 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, true));

        String original1 = VariationUnparser.undiff(original, Time.BEFORE);
        String original2 = VariationUnparser.undiff(original, Time.AFTER);
        String unparsed11 = VariationUnparser.undiff(unparsed1, Time.BEFORE);
        String unparsed12 = VariationUnparser.undiff(unparsed1, Time.AFTER);
        String unparsed21 = VariationUnparser.undiff(unparsed2, Time.BEFORE);
        String unparsed22 = VariationUnparser.undiff(unparsed2, Time.AFTER);
        String unparsed31 = VariationUnparser.undiff(unparsed3, Time.BEFORE);
        String unparsed32 = VariationUnparser.undiff(unparsed3, Time.AFTER);
        String unparsed41 = VariationUnparser.undiff(unparsed4, Time.BEFORE);
        String unparsed42 = VariationUnparser.undiff(unparsed4, Time.AFTER);

        assertEquals(removeWhitespace(original1, false), removeWhitespace(unparsed11, false));
        assertEquals(removeWhitespace(original2, false), removeWhitespace(unparsed12, false));
        assertEquals(removeWhitespace(original1, false), removeWhitespace(unparsed21, false));
        assertEquals(removeWhitespace(original2, false), removeWhitespace(unparsed22, false));
        assertEquals(removeWhitespace(original1, false), removeWhitespace(unparsed31, false));
        assertEquals(removeWhitespace(original2, false), removeWhitespace(unparsed32, false));
        assertEquals(removeWhitespace(original1, false), removeWhitespace(unparsed41, false));
        assertEquals(removeWhitespace(original2, false), removeWhitespace(unparsed42, false));
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
