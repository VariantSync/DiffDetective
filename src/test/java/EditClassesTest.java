import org.junit.Assert;
import org.junit.Test;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;

import java.io.IOException;
import java.nio.file.Path;

public class EditClassesTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("patterns");

    @Test
    public void testAtomics() throws IOException {
        final Path path = testDir.resolve("elementary.diff");
        final DiffTree t = DiffTree.fromFile(path, false, true).unwrap().getSuccess();
        t.forAll(node -> {
            if (node.isArtifact()) {
                Assert.assertEquals(
                        node.getLabel(),
                        ProposedEditClasses.Instance.match(node).getName()
                );
            }
        });
    }
}
