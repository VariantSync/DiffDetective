import org.junit.Assert;
import org.junit.Test;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.pattern.elementary.proposed.ProposedElementaryPatterns;

import java.io.IOException;
import java.nio.file.Path;

public class ElementaryPatternsTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("patterns");

    @Test
    public void testAtomics() throws IOException {
        final Path path = testDir.resolve("elementary.diff");
        final DiffTree t = DiffTree.fromFile(path, false, true).unwrap().getSuccess();
        t.forAll(node -> {
            if (node.isArtifact()) {
                Assert.assertEquals(
                        node.getLabel(),
                        ProposedElementaryPatterns.Instance.match(node).getName()
                );
            }
        });
    }
}
