import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.datasets.predefined.StanciulescuMarlin;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.mining.VariationDiffMiner;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.variation.diff.render.RenderOptions;
import org.variantsync.diffdetective.variation.diff.serialize.GraphFormat;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphConstants;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.TypeDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
import org.variantsync.diffdetective.variation.diff.transform.VariationDiffTransformer;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

@Disabled
public class TreeTransformersTest {
    private static final boolean RENDER = true;
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/collapse");
    private static final Path genDir = resDir.resolve("gen");
    private static final RenderOptions<DiffLinesLabel> renderOptions = new RenderOptions<>(
            GraphFormat.VARIATION_DIFF,
            new CommitDiffVariationDiffLabelFormat(),
            new TypeDiffNodeFormat<>(),
            new DefaultEdgeLabelFormat<>(),
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

    private void transformAndRender(String diffFileName) throws IOException, DiffParseException {
        final VariationDiff<DiffLinesLabel> t = VariationDiff.fromFile(resDir.resolve(diffFileName), new VariationDiffParseOptions(true, true));
        transformAndRender(t, diffFileName, "0", null);
    }

    private void transformAndRender(VariationDiff<DiffLinesLabel> variationDiff, String name, String commit, Repository repository) {
        final VariationDiffRenderer renderer = VariationDiffRenderer.WithinDiffDetective();
        final String treeName = name + LineGraphConstants.TREE_NAME_SEPARATOR + commit;

        INFO.accept("Original State");
        if (RENDER) {
            renderer.render(variationDiff, treeName + "_0", genDir, renderOptions);
        }

        int i = 1;
        int prevSize = variationDiff.computeSize();
        final List<VariationDiffTransformer<DiffLinesLabel>> transformers = VariationDiffMiner.Postprocessing(repository);
        for (final VariationDiffTransformer<DiffLinesLabel> f : transformers) {
            INFO.accept("Applying transformation " + f + ".");
            f.transform(variationDiff);

            final int currentSize = variationDiff.computeSize();
            if (prevSize != currentSize) {
                INFO.accept((currentSize < prevSize ? "Reduced" : "Increased") + " the number of nodes from " + prevSize + " to " + currentSize + ".");
            }
            prevSize = currentSize;

            variationDiff.assertConsistency();

            if (RENDER) {
                renderer.render(variationDiff, treeName + "_" + i, genDir, renderOptions);
            }
            ++i;
        }
    }

    @BeforeEach
    public void init() {
//        Main.setupLogger(Level.INFO);
//        VariationDiffTransformer.checkDependencies(transformers);
    }

    @Test
    public void simpleTest() throws IOException, DiffParseException {
        transformAndRender("simple.txt");
    }

    @Test
    public void elifTest() throws IOException, DiffParseException {
        transformAndRender("elif.txt");
    }

    private void testCommit(String file, String commitHash) throws IOException {
        final Repository marlin = StanciulescuMarlin.fromZipInDiffDetectiveAt(Path.of("."));
        final PatchDiff patch = VariationDiffParser.parsePatch(marlin, file, commitHash);
        assertNotNull(patch);
        transformAndRender(patch.getVariationDiff(), file, commitHash, marlin);
    }

    @Test
    public void testWurmcoil() throws IOException {
        testCommit("Marlin/pins.h", "d6d6fb8930be8d0b3bd34592c915732937c6f4d9");
    }

    @Test
    public void testConfiguration_adv() throws IOException {
        testCommit("Marlin/example_configurations/RigidBot/Configuration_adv.h", "d3fe3a0962fdbdcd9548abaf765e0cff72d9cf8d");
    }

    @Test
    public void test_pins_SANGUINOLOLU_11() throws IOException {
        testCommit("Marlin/pins_SANGUINOLOLU_11.h", "d3fe3a0962fdbdcd9548abaf765e0cff72d9cf8d");
    }

    @Test
    public void test_pins_RAMPS_13() throws IOException {
        testCommit("Marlin/pins_RAMPS_13.h", "d882e1aee7fb4e4afb43445899b477caf1fffce3");
    }

    @Test
    public void test_SanityCheck() throws IOException {
        testCommit("Marlin/SanityCheck.h", "cbd582865e2a76b7be3b03533a0e06e8daf76f15");
    }

    @Test
    public void test_pins_MINIRAMBO() throws IOException {
        testCommit("Marlin/pins_MINIRAMBO.h", "50f1a8fd92b351bf1fa29e5cd31f24fc884999c0");
    }
}
