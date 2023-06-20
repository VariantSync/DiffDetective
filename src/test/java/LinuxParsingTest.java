import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;

import java.io.IOException;
import java.nio.file.Path;

@Deprecated
public class LinuxParsingTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("linux");

//    @Test
    public void test1() throws IOException, DiffParseException {
        final String testFilename = "test1.diff";
        final Path path = testDir.resolve(testFilename);
        final VariationDiff<DiffLinesLabel> t = VariationDiff.fromFile(path, new VariationDiffParseOptions(false, true));

//        new FeatureExpressionFilter(LinuxKernel::isFeature).transform(t);

        t.forAll(n -> {
            if (n.isAnnotation()) {
                Assert.assertTrue(n.getLabel().toString().contains("CONFIG_"), () -> "Macro node " + n + " is not a feature annotation!");
            }
        });

        VariationDiffRenderer r = VariationDiffRenderer.WithinDiffDetective();
        r.render(t, testFilename, testDir.resolve("gen"));
    }
}
