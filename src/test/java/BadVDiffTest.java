import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.bad.BadVDiff;

import java.io.IOException;
import java.nio.file.Path;

public class BadVDiffTest {
    private static final Path resDir = Constants.RESOURCE_DIR.resolve("bad");
    @ParameterizedTest
    @ValueSource(strings = { "1.diff" })
    public void test(String filename) throws IOException, DiffParseException {
        final Path testfile = resDir.resolve(filename);
        final DiffTree d = DiffTree.fromFile(testfile, false, false);
        final BadVDiff b = BadVDiff.fromGood(d);
        System.out.println(b.prettyPrint());
    }
}
