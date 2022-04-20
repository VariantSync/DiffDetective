import org.junit.Assert;
import org.junit.Test;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.Starfold;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

public class StarfoldTest {
    private static final Path RESOURCE_DIR = Constants.RESOURCE_DIR.resolve("starfold");

    private record TestCase(Path inputDiff, Path expectedDiffRespectingNodeOrder, Path expectedDiffIgnoringNodeOrder) {
        TestCase(final String filename) {
            this(
                    RESOURCE_DIR.resolve(filename),
                    RESOURCE_DIR.resolve("expected").resolve("respectNodeOrder").resolve(filename),
                    RESOURCE_DIR.resolve("expected").resolve("ignoreNodeOrder").resolve(filename)
            );
        }
    }

    private static final List<TestCase> TEST_CASES = Stream.of(
            "2x2", "nesting1"
    ).map(s -> s + ".diff").map(TestCase::new).toList();

    private void test(final Starfold starfold, Function<TestCase, Path> getExpectedResultFile) throws IOException {
        for (TestCase testCase : TEST_CASES) {
            final DiffTree t = DiffTree.fromFile(testCase.inputDiff, true, true).unwrap().getSuccess();
            starfold.transform(t);

            Assert.assertEquals(
                    FileUtils.normalizedLineEndings(t.toTextDiff().trim()),
                    FileUtils.normalizedLineEndings(FileUtils.readUTF8(getExpectedResultFile.apply(testCase))).trim()
            );
        }
    }

    @Test
    public void testRespectNodeOrder() throws IOException {
        test(Starfold.RespectNodeOrder(), TestCase::expectedDiffRespectingNodeOrder);
    }

    @Test
    public void testIgnoreNodeOrder() throws IOException {
        test(Starfold.IgnoreNodeOrder(), TestCase::expectedDiffIgnoringNodeOrder);
    }
}
