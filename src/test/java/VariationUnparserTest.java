import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.diffdetective.variation.VariationUnparser;

public class VariationUnparserTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("unparser");

    private final static String testCaseSuffix = ".txt";

    protected static Stream<Path> findTestCases(Path dir) {
        try {
            return Files
                    .list(dir)
                    .filter(filename -> filename.getFileName().toString().endsWith(testCaseSuffix));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Disabled
    @Test
    public void tests() {
        findTestCases(testDir).forEach(this::test);
    }

    @Disabled
    @Test
    public void teststest() {
        Path path = testDir.resolve("test2.txt");
        String temp = "b";
        VariationTree<DiffLinesLabel> tree = null;
        try {
            tree = VariationTree.fromFile(path);
            temp = VariationUnparser.variationTreeUnparser(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        assertNotNull(tree.root().getChildren().get(0).getEndIf());
    }

    public void test(Path basename) {
        testCase(basename);
    }

    public static void testCase(Path testCasePath) {
        String temp = "";
        try {
            temp = Files.readString(testCasePath);
        } catch (Exception e) {
            e.printStackTrace();
        }
        temp = temp.replaceAll("\\r\\n", "\n");
        String unparse = parseUnparse(testCasePath);
        assertEquals(temp, unparse);
    }

    public static String parseUnparse(Path path) {
        String temp = "b";
        try {
            VariationTree<DiffLinesLabel> tree = VariationTree.fromFile(path);
            temp = VariationUnparser.variationTreeUnparser(tree);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return temp;
    }

}
