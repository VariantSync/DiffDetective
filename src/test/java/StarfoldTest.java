import org.junit.Assert;
import org.junit.Test;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.Starfold;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

public class StarfoldTest {
    private static final Path RESOURCE_DIR = Constants.RESOURCE_DIR.resolve("starfold");

    private record TestCase(Path inputDiff, Path expectedDiff) {
        TestCase(final String name) {
            this(
                    RESOURCE_DIR.resolve(name + ".diff"),
                    RESOURCE_DIR.resolve(name + ".expected.diff")
            );
        }
    }

    private static final List<TestCase> TEST_CASES = Stream.of(
            "2x2", "nesting1"
    ).map(TestCase::new).toList();

    @Test
    public void testAll() throws IOException {
        for (TestCase testCase : TEST_CASES) {
            final DiffTree t = DiffTree.fromFile(testCase.inputDiff, true, true).unwrap().getSuccess();
            Starfold.RespectNodeOrder().transform(t);
//            System.out.println(t.toTextDiff());
            Assert.assertEquals(
                    t.toTextDiff().trim(),
                    FileUtils.readUTF8(testCase.expectedDiff).trim()
            );
        }
    }
}
