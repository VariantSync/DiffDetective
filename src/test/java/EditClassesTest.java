import org.junit.jupiter.api.Test;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.editclass.proposed.ProposedEditClasses;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.IOException;
import java.nio.file.Path;

public class EditClassesTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("patterns");

    @Test
    public void testAtomics() throws IOException, DiffParseException {
        final Path path = testDir.resolve("elementary.diff");
        final DiffTree<DiffLinesLabel> t = DiffTree.fromFile(path, new DiffTreeParseOptions(false, true));
        t.forAll(node -> {
            if (node.isArtifact()) {
                assertEquals(
                        node.getLabel().toString(),
                        ProposedEditClasses.Instance.match(node).getName()
                );
            }
        });
    }
}
