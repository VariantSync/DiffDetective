import org.junit.BeforeClass;
import org.junit.Test;
import org.prop4j.And;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.RenderOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.LineGraphImport;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.MappingsDiffNodeFormat;
import org.variantsync.diffdetective.main.SimpleRenderer;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.variantsync.diffdetective.util.fide.FormulaUtils.negate;

public class FeatureSplitTest {

    private static List<Path> TEST_FILES;
    private static List<DiffTree> DIFF_TREES;

    @BeforeClass
    public static void init() throws IOException {
        // Generate DiffTrees based on the TEST_FILES
        TEST_FILES = Files.list(Paths.get("src/test/resources/feature_split")).toList();
        for (final Path testFile : TEST_FILES) {
            Logger.info("Testing {}", testFile);
            // create variation tree diff
            final DiffTree diffTree = DiffTree.fromFile(testFile, false, true).unwrap().getSuccess();
            Logger.info("Gathered diff \n {}", diffTree.toTextDiff());

            DIFF_TREES.add(diffTree);
        }
    }

    /**
     * Check validity of variation tree diff of imported diff
     */
    @Test
    public void visualizeDiff() throws IOException {
        //TODO render ever diff
    }

    /**
     * Check if valid subtrees are generated
     */
    @Test
    public void generateSubtreeTest() throws IOException {
        //TODO render ever diff
    }

    //var treeDiff = DiffTree.fromDiff("", false, false);
    // Display Diff
    //private final static PCTest.TestCase a = new PCTest.TestCase(
    //        Path.of("a.diff"),
    //        Map.of(
    //                "1", new PCTest.ExpectedPC(A, new And(A, B)),
    //                "2", new PCTest.ExpectedPC(A, new And(A, C, negate(B))),
    //                "3", new PCTest.ExpectedPC(new And(A, D, E), new And(A, D)),
    //                "4", new PCTest.ExpectedPC(A, A)
    //        ));

}
