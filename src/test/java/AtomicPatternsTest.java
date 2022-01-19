import diff.difftree.DiffTree;
import org.junit.Assert;
import org.junit.Test;
import pattern.atomic.proposed.ProposedAtomicPatterns;

import java.io.IOException;
import java.nio.file.Path;

public class AtomicPatternsTest {
    private final static Path testDir = Constants.RESOURCE_DIR.resolve("patterns");

    @Test
    public void testAtomics() throws IOException {
        final Path path = testDir.resolve("atomics.diff");
        final DiffTree t = DiffTree.fromFile(path, false, true).unwrap().getSuccess();
        t.forAll(node -> {
            if (node.isCode()) {
                Assert.assertEquals(
                        node.getLabel(),
                        ProposedAtomicPatterns.Instance.match(node).getName()
                );
            }
        });
    }
}
