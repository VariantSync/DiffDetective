import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.datasets.predefined.StanciulescuMarlin;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.render.RenderOptions;
import org.variantsync.diffdetective.diff.difftree.serialize.GraphFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.TypeDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.transform.DiffTreeTransformer;
import org.variantsync.diffdetective.mining.DiffTreeMiner;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

public class TreeTransformersTest {
    private static final boolean RENDER = true;
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/collapse");
    private static final Path genDir = resDir.resolve("gen");
    private static final RenderOptions renderOptions = new RenderOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new TypeDiffNodeFormat(),
            new DefaultEdgeLabelFormat(),
            false,
            500,
            50,
            0.3,
            3,
            3,
            true,
            List.of()
            );

    private static final Consumer<String> INFO = System.out::println;

    private void transformAndRender(String diffFileName) throws IOException {
        final DiffTree t = DiffTree.fromFile(resDir.resolve(diffFileName), true, true).unwrap().getSuccess();
        transformAndRender(t, diffFileName, "0", null);
    }

    private void transformAndRender(DiffTree diffTree, String name, String commit, Repository repository) {
        final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
        final String treeName = name + LineGraphConstants.TREE_NAME_SEPARATOR + commit;

        INFO.accept("Original State");
        if (RENDER) {
            renderer.render(diffTree, treeName + "_0", genDir, renderOptions);
        }

        int i = 1;
        int prevSize = diffTree.computeSize();
        final List<DiffTreeTransformer> transformers = DiffTreeMiner.Postprocessing(repository);
        for (final DiffTreeTransformer f : transformers) {
            INFO.accept("Applying transformation " + f + ".");
            f.transform(diffTree);

            final int currentSize = diffTree.computeSize();
            if (prevSize != currentSize) {
                INFO.accept((currentSize < prevSize ? "Reduced" : "Increased") + " the number of nodes from " + prevSize + " to " + currentSize + ".");
            }
            prevSize = currentSize;

            diffTree.assertConsistency();

            if (RENDER) {
                renderer.render(diffTree, treeName + "_" + i, genDir, renderOptions);
            }
            ++i;
        }
    }

    @Before
    public void init() {
//        Main.setupLogger(Level.INFO);
//        DiffTreeTransformer.checkDependencies(transformers);
    }

//    @Test
    @Ignore
    public void simpleTest() throws IOException {
        transformAndRender("simple.txt");
    }

//    @Test
    @Ignore
    public void elifTest() throws IOException {
        transformAndRender("elif.txt");
    }

    private void testCommit(String file, String commitHash) throws IOException {
        final Repository marlin = StanciulescuMarlin.fromZipInDiffDetectiveAt(Path.of("."));
        final PatchDiff patch = DiffTreeParser.parsePatch(marlin, file, commitHash);
        Assert.assertNotNull(patch);
        transformAndRender(patch.getDiffTree(), file, commitHash, marlin);
    }

//    @Test
    @Ignore
    public void testWurmcoil() throws IOException {
        testCommit("Marlin/pins.h", "d6d6fb8930be8d0b3bd34592c915732937c6f4d9");
    }

//    @Test
    @Ignore
    public void testConfiguration_adv() throws IOException {
        testCommit("Marlin/example_configurations/RigidBot/Configuration_adv.h", "d3fe3a0962fdbdcd9548abaf765e0cff72d9cf8d");
    }

//    @Test
    @Ignore
    public void test_pins_SANGUINOLOLU_11() throws IOException {
        testCommit("Marlin/pins_SANGUINOLOLU_11.h", "d3fe3a0962fdbdcd9548abaf765e0cff72d9cf8d");
    }

//    @Test
    @Ignore
    public void test_pins_RAMPS_13() throws IOException {
        testCommit("Marlin/pins_RAMPS_13.h", "d882e1aee7fb4e4afb43445899b477caf1fffce3");
    }

//    @Test
    @Ignore
    public void test_SanityCheck() throws IOException {
        testCommit("Marlin/SanityCheck.h", "cbd582865e2a76b7be3b03533a0e06e8daf76f15");
    }

//    @Test
    @Ignore
    public void test_pins_MINIRAMBO() throws IOException {
        testCommit("Marlin/pins_MINIRAMBO.h", "50f1a8fd92b351bf1fa29e5cd31f24fc884999c0");
    }
}
