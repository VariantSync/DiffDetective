import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.Duplication;
import org.variantsync.diffdetective.diff.difftree.transform.FeatureSplit;

import java.awt.*;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
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
        DiffNode node = DIFF_TREES.get(0).getRoot().getAllChildren().get(1);
        DiffTree initDiffTree = DIFF_TREES.get(0);
        DiffTree tree = new Duplication().deepClone(initDiffTree);
        DiffTree subtree = FeatureSplit.generateSubtree(node, initDiffTree);

        List<Integer> toRemove = new ArrayList<>(DIFF_TREES.get(0).getRoot().getAllChildren().get(0).getAllChildren().stream().map(DiffNode::getID).toList());
        toRemove.remove(2);
        toRemove.forEach(elem -> tree.computeAllNodesThat(treeElem -> treeElem.getID() == elem).forEach(DiffNode::drop));

        Assert.assertEquals(initDiffTree, subtree);
    }

    @Test
    public void shallowCloneTest() {
        DiffNode node = DIFF_TREES.get(0).getRoot().getAllChildren().get(0);
        Logger.info(node.toString());
        DiffNode duplication = Duplication.shallowClone(node);
        Logger.info(duplication.toString());
        Assert.assertEquals(node, duplication);
    }

    @Test
    public void deepCloneSubtreeTest() {
        DiffTree tree = DIFF_TREES.get(0);
        HashMap<Integer, DiffNode> originalHashmap = new HashMap<>();
        tree.forAll(node -> originalHashmap.put(node.getID(), node));
        HashMap<Integer, DiffNode> duplication = new Duplication().deepCloneAsHashmap(tree.getRoot());
        Assert.assertEquals(originalHashmap, duplication);
    }

}

