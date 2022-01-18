import datasets.DefaultRepositories;
import de.variantsync.functjonal.Result;
import diff.difftree.DiffTree;
import diff.difftree.render.DiffTreeRenderer;
import diff.difftree.transform.FeatureExpressionFilter;
import diff.result.DiffError;
import org.junit.Test;
import util.Assert;

import java.io.IOException;
import java.nio.file.Path;

public class LinuxParsingTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("linux");

    @Test
    public void test1() throws IOException {
        final String testFilename = "test1.diff";
        final Path path = testDir.resolve(testFilename);
        final Result<DiffTree, DiffError> parseResult =
                DiffTree.fromFile(path, false, true).unwrap();

        if (parseResult.isFailure()) {
            throw new AssertionError("Could not parse " + path + " because " + parseResult.getFailure());
        }

        final DiffTree t = parseResult.getSuccess();
        new FeatureExpressionFilter(DefaultRepositories.LINUX_FEATURE_EXPRESSION_FILTER).transform(t);

        t.forAll(n -> {
            if (n.isMacro()) {
                Assert.assertTrue(n.getLabel().contains("CONFIG_"), () -> "Macro node " + n + " is not a feature annotation!");
            }
        });

        DiffTreeRenderer r = DiffTreeRenderer.WithinDiffDetective();
        r.render(t, testFilename, testDir.resolve("gen"));
    }
}
