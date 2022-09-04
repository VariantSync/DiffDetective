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
import java.util.*;
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
     * test the operator featureSplit
     */
    @Test
    public void featureSplitTest() {
        DiffTree tree = DIFF_TREES.get(0);
        FeatureSplit featureSplit = new FeatureSplit();
        HashMap<String, DiffTree> featureAwareTrees = featureSplit.featureSplit(tree, Arrays.asList("Unix", "Get"));

        Assert.assertEquals(featureAwareTrees.get("Get").getRoot().getAllChildren().get(0).getAllChildren().size(), 2);
        Assert.assertEquals(featureAwareTrees.get("Unix").getRoot().getAllChildren().get(0).getAllChildren().size(), 1);
    }

    @Test
    public void featureSplitTest2() {
        DiffTree tree = DIFF_TREES.get(0);
        FeatureSplit featureSplit = new FeatureSplit();
        HashMap<String, DiffTree> featureAwareTrees = featureSplit.featureSplit(tree, "Get");

        Assert.assertEquals(featureAwareTrees.get("Get").getRoot().getAllChildren().get(0).getAllChildren().size(), 3);
    }


    /**
     * Check if valid subtrees are generated
     */
    @Test
    public void generateClustersTest() {
        DiffTree tree = DIFF_TREES.get(0);
        FeatureSplit featureSplit = new FeatureSplit();
        List<DiffTree> subtrees = featureSplit.generateAllSubtrees(tree);
        HashMap<String, List<DiffTree>> clusters = featureSplit.generateClusters(subtrees, Arrays.asList("Unix", "Get"));

        Assert.assertEquals(clusters.get("Unix").size(), 1);
        Assert.assertEquals(clusters.get("Get").size(), 2);
        Assert.assertEquals(clusters.get("remains").size(), 0);
    }

    /**
     * Check if valid subtrees are generated
     */
    @Test
    public void generateAllSubtreesTest() {
        DiffTree tree = DIFF_TREES.get(0);
        FeatureSplit featureSplit = new FeatureSplit();
        List<DiffTree> subtrees = featureSplit.generateAllSubtrees(tree);

        Assert.assertEquals(subtrees.size(), 3);
    }

    @Test
    public void generateSubtreeTest() {
        DiffNode node = DIFF_TREES.get(0).getRoot().getAllChildren().get(1);
        DiffTree initDiffTree = DIFF_TREES.get(0);
        DiffTree tree = new Duplication().deepClone(initDiffTree);
        DiffTree subtree = FeatureSplit.generateSubtree(node, initDiffTree);

        List<Integer> toRemove = new ArrayList<>(DIFF_TREES.get(0).getRoot().getAllChildren().get(0).getAllChildren().stream().map(DiffNode::getID).toList());
        toRemove.remove(2);
        toRemove.forEach(elem -> tree.computeAllNodesThat(treeElem -> treeElem.getID() == elem).forEach(DiffNode::drop));

        // TODO: compare Trees directly, and not in a Hashmap format

        HashMap<Integer, DiffNode> originalHashmap = new HashMap<>();
        tree.forAll(elem -> originalHashmap.put(elem.getID(), elem));

        HashMap<Integer, DiffNode> duplicatedHashmap = new HashMap<>();
        subtree.forAll(elem -> duplicatedHashmap.put(elem.getID(), elem));

        Assert.assertEquals(originalHashmap, duplicatedHashmap);
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

