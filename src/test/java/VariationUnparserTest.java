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

import static org.variantsync.diffdetective.experiments.thesis_es.UnparseAnalysis.removeWhitespace;

public class VariationUnparserTest {
    private final static Path testDirTree = Constants.RESOURCE_DIR.resolve("unparser");

    private final static Path testDirDiff = Constants.RESOURCE_DIR.resolve("diffs").resolve("parser");
    private final static String testCaseSuffixTree = ".txt";

    private final static String testCaseSuffixDiff = ".diff";

    protected static Stream<Path> findTestCases(Path dir, String testCaseSuffix) {
        try {
            return Files
                    .list(dir)
                    .filter(filename -> filename.getFileName().toString().endsWith(testCaseSuffix));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Stream<Path> testsTree() throws IOException {
        return findTestCases(testDirTree, testCaseSuffixTree);
    }

    public static Stream<Path> testsDiff() throws IOException {
        return findTestCases(testDirDiff, testCaseSuffixDiff);
    }

    @ParameterizedTest
    @MethodSource("testsTree")
    public void testTreeDir(Path basename) throws IOException, DiffParseException {
        testCaseTree(basename);
    }

    @ParameterizedTest
    @MethodSource("testsDiff")
    public void testDiffDir(Path basename) throws IOException, DiffParseException {
        testCaseDiff(basename);
    }

    @Test
    public void testDiff() {
        String source = "";
        String temp = "";
        try {
            source = Files.readString(Path.of("src", "test", "resources", "unparser", "test8.txt"));
            VariationTree<DiffLinesLabel> tree = VariationTree.fromText(source, VariationTreeSource.Unknown,
                    VariationDiffParseOptions.Default);
            temp = VariationUnparser.variationTreeUnparser(tree);
            System.out.println(removeWhitespace(source).equals(removeWhitespace(temp)));
            // System.out.println(removeWhitespace(source));
            // System.out.println("Ende");
            // System.out.println(removeWhitespace(temp));
            // System.out.println("Ende");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void testCaseTree(Path testCasePath) {
        String temp = "";
        try {
            temp = Files.readString(testCasePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(testCasePath);
        temp = temp.replaceAll("\\r\\n", "\n");
        String unparse1 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, false));
        String unparse2 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(false, true));
        String unparse3 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, false));
        String unparse4 = parseUnparseTree(testCasePath, new VariationDiffParseOptions(true, true));
        System.out.println(temp.equals(unparse1) + " " + temp.equals(unparse2) + " " + temp.equals(unparse3) + " "
                + temp.equals(unparse4));
        temp = removeWhitespace(temp);
        unparse1 = removeWhitespace(unparse1);
        unparse2 = removeWhitespace(unparse2);
        unparse3 = removeWhitespace(unparse3);
        unparse4 = removeWhitespace(unparse4);
        System.out.println(temp.equals(unparse1) + " " + temp.equals(unparse2) + " " + temp.equals(unparse3) + " "
                + temp.equals(unparse4));
    }

    public static void testCaseDiff(Path testCasePath) {
        String temp = "";
        try {
            temp = Files.readString(testCasePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println(testCasePath);
        temp = temp.replaceAll("\\r\\n", "\n");
        String unparse1 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, false));
        String unparse2 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(false, true));
        String unparse3 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, false));
        String unparse4 = parseUnparseDiff(testCasePath, new VariationDiffParseOptions(true, true));
        System.out.println(temp.equals(unparse1) + " " + temp.equals(unparse2) + " " + temp.equals(unparse3) + " "
                + temp.equals(unparse4));
        String temp1 = VariationUnparser.undiff(temp, Time.BEFORE);
        String temp2 = VariationUnparser.undiff(temp, Time.AFTER);
        String unparse11 = VariationUnparser.undiff(unparse1, Time.BEFORE);
        String unparse12 = VariationUnparser.undiff(unparse1, Time.AFTER);
        String unparse21 = VariationUnparser.undiff(unparse2, Time.BEFORE);
        String unparse22 = VariationUnparser.undiff(unparse2, Time.AFTER);
        String unparse31 = VariationUnparser.undiff(unparse3, Time.BEFORE);
        String unparse32 = VariationUnparser.undiff(unparse3, Time.AFTER);
        String unparse41 = VariationUnparser.undiff(unparse1, Time.BEFORE);
        String unparse42 = VariationUnparser.undiff(unparse1, Time.AFTER);
        System.out.println(temp1.equals(unparse11) + " " + temp2.equals(unparse12) + " " + temp1.equals(unparse21) + " "
                + temp2.equals(unparse22) + " " + temp1.equals(unparse31) + " " + temp2.equals(unparse32) + " "
                + temp1.equals(unparse41) + " " + temp2.equals(unparse42));
        System.out.println(removeWhitespace(temp1).equals(removeWhitespace(unparse11)) + " "
                + removeWhitespace(temp2).equals(removeWhitespace(unparse12)) + " "
                + removeWhitespace(temp1).equals(removeWhitespace(unparse21)) + " "
                + removeWhitespace(temp2).equals(removeWhitespace(unparse22)) + " "
                + removeWhitespace(temp1).equals(removeWhitespace(unparse31)) + " "
                + removeWhitespace(temp2).equals(removeWhitespace(unparse32)) + " "
                + removeWhitespace(temp1).equals(removeWhitespace(unparse41)) + " "
                + removeWhitespace(temp2).equals(removeWhitespace(unparse42)));
        temp = removeWhitespace(temp);
        unparse1 = removeWhitespace(unparse1);
        unparse2 = removeWhitespace(unparse2);
        unparse3 = removeWhitespace(unparse3);
        unparse4 = removeWhitespace(unparse4);
        System.out.println(temp.equals(unparse1) + " " + temp.equals(unparse2) + " " + temp.equals(unparse3) + " "
                + temp.equals(unparse4));
    }

    public static String parseUnparseTree(Path path, VariationDiffParseOptions option) {
        String temp = "b";
        try {
            VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path, option);
            temp = VariationUnparser.variationTreeUnparser(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

    public static String parseUnparseDiff(Path path, VariationDiffParseOptions option) {
        String temp = "b";
        try {
            VariationDiff<DiffLinesLabel> diff = VariationDiff.fromFile(path, option);
            temp = VariationUnparser.variationDiffUnparser(diff);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }
}
