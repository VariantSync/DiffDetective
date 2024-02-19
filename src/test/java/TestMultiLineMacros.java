import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;

import java.io.IOException;
import java.nio.file.Path;
import java.util.stream.Stream;

public class TestMultiLineMacros {

    private final static Path testDir = Constants.RESOURCE_DIR.resolve("multilinemacros");

    public static Stream<Path> multilineTests() throws IOException {
        return VariationDiffParserTest.findTestCases(testDir);
    }

    @ParameterizedTest
    @MethodSource("multilineTests")
    public void testMultiline(Path basename) throws IOException, DiffParseException {
        VariationDiffParserTest.testCase(basename);
    }
}
