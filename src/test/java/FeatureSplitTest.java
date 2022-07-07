import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.Duplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class FeatureSplitTest {

    private static final List<DiffTree> DIFF_TREES = new ArrayList<>();

    @BeforeClass
    public static void init() throws IOException {
        // Generate DiffTrees based on the TEST_FILES
        List<Path> TEST_FILES = Files.list(Paths.get("src/test/resources/feature_split/")).toList();
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
    public void visualizeDiff() {
        //TODO render ever diff
    }

    /**
     * Check if valid subtrees are generated
     */
    @Test
    public void generateSubtreeTest() {
        //TODO render ever diff
    }

    @Test
    public void shallowCloneTest() {
        DiffNode node = DIFF_TREES.get(0).getRoot().getAllChildren().get(0);
        Logger.info(node.toString());
        DiffNode duplication = new Duplication().shallowClone(node);
        Logger.info(duplication.toString());
        Assert.assertEquals(node, duplication);
    }

    @Test
    public void deepCloneTest() {
        DiffNode node = DIFF_TREES.get(0).getRoot().getAllChildren().get(0);
        Logger.info(node.toString());
        DiffTree duplication = new Duplication().deepClone(node);
        Logger.info(duplication.toString());
        Assert.assertEquals(node, duplication);
    }

}
