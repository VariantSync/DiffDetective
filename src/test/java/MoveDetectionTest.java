import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.transform.NaiveMovedArtifactDetection;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.IOException;
import java.nio.file.Path;

public class MoveDetectionTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/move");
    private static final Path genDir = resDir.resolve("gen");

//    @Test
    public void simpleTest() throws IOException, DiffParseException {
        final DiffTree t = DiffTree.fromFile(resDir.resolve("simple.txt"), true, true);
        final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
        renderer.render(t, "MoveDetectionTestSimpleTest_Before", genDir);
        new NaiveMovedArtifactDetection().transform(t);
        renderer.render(t, "MoveDetectionTestSimpleTest_After", genDir);
    }
}
