import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Result;

import java.io.IOException;
import java.nio.file.Path;

@Deprecated
public class LinuxParsingTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("linux");

//    @Test
    public void test1() throws IOException {
        final String testFilename = "test1.diff";
        final Path path = testDir.resolve(testFilename);
        final Result<DiffTree, DiffError> parseResult =
                DiffTree.fromFile(path, false, true).unwrap();

        if (parseResult.isFailure()) {
            throw new AssertionError("Could not parse " + path + " because " + parseResult.getFailure());
        }

        final DiffTree t = parseResult.getSuccess();
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
