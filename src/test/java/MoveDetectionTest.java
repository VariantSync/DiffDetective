import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.render.DiffTreeRenderer;
import org.variantsync.diffdetective.diff.difftree.transform.NaiveMovedCodeDetection;

import java.io.IOException;
import java.nio.file.Path;

public class MoveDetectionTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("diffs/move");
    private static final Path genDir = resDir.resolve("gen");

//    @Test
    public void simpleTest() throws IOException {
        final DiffTree t = DiffTree.fromFile(resDir.resolve("simple.txt"), true, true).unwrap().getSuccess();
        final DiffTreeRenderer renderer = DiffTreeRenderer.WithinDiffDetective();
        renderer.render(t, "MoveDetectionTestSimpleTest_Before", genDir);
        new NaiveMovedCodeDetection().transform(t);
        renderer.render(t, "MoveDetectionTestSimpleTest_After", genDir);
    }
}
