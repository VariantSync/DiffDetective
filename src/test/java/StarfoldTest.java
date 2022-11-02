import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.Starfold;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class StarfoldTest {
    private static final Path INPUT_DIR = Constants.RESOURCE_DIR.resolve("starfold");
    private static final Path EXPECTED_DIR = INPUT_DIR.resolve("expected");

    public static Stream<String> testCases() {
        return Stream
            .of("2x2", "nesting1")
            .map(s -> s + ".diff");
    }

    private void test(Path inputDiff, final Starfold starfold, Path expected) throws IOException, DiffParseException {
        final DiffTree t = DiffTree.fromFile(inputDiff, true, true);
        starfold.transform(t);
        TestUtils.assertEqualToFile(
                expected,
                t.toTextDiff().trim()
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testRespectNodeOrder(String filename) throws IOException, DiffParseException {
        test(
            INPUT_DIR.resolve(filename),
            Starfold.RespectNodeOrder(),
            EXPECTED_DIR.resolve("respectNodeOrder").resolve(filename)
        );
    }

    @ParameterizedTest
    @MethodSource("testCases")
    public void testIgnoreNodeOrder(String filename) throws IOException, DiffParseException {
        test(
            INPUT_DIR.resolve(filename),
            Starfold.IgnoreNodeOrder(),
            EXPECTED_DIR.resolve("ignoreNodeOrder").resolve(filename)
        );
    }
}
