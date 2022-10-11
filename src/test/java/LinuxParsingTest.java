import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
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
        final DiffTree t = DiffTree.fromFile(path, false, true);

//        new FeatureExpressionFilter(LinuxKernel::isFeature).transform(t);

        t.forAll(n -> {
            if (n.isAnnotation()) {
                Assert.assertTrue(n.getLabel().contains("CONFIG_"), () -> "Macro node " + n + " is not a feature annotation!");
            }
        });

        DiffTreeRenderer r = DiffTreeRenderer.WithinDiffDetective();
        r.render(t, testFilename, testDir.resolve("gen"));
    }
}
