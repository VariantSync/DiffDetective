import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.render.VariationDiffRenderer;
import org.variantsync.diffdetective.variation.diff.transform.NaiveMovedArtifactDetection;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.IOException;
import java.nio.file.Path;

public class MoveDetectionTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/move");
    private static final Path genDir = resDir.resolve("gen");

//    @Test
    public void simpleTest() throws IOException, DiffParseException {
        final VariationDiff<DiffLinesLabel> t = VariationDiff.fromFile(resDir.resolve("simple.txt"), new VariationDiffParseOptions(true, true));
        final VariationDiffRenderer renderer = VariationDiffRenderer.WithinDiffDetective();
        renderer.render(t, "MoveDetectionTestSimpleTest_Before", genDir);
        new NaiveMovedArtifactDetection<DiffLinesLabel>().transform(t);
        renderer.render(t, "MoveDetectionTestSimpleTest_After", genDir);
    }
}
