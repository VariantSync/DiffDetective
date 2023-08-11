import org.apache.commons.io.IOUtils;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;

public final class TestUtils {
    /**
     * Compare two line graphs.
     *
     * @param originalLineGraph The original line graph
     * @param generatedLineGraph The generated line graph
     */
    public static void assertEqualToFile(final Path filePath, final String actual) throws IOException {
        try (BufferedReader expected = Files.newBufferedReader(filePath)) {
            assertTrue(
                    IOUtils.contentEqualsIgnoreEOL(expected, new StringReader(actual)),
                    "expected content of " + filePath + " but was:<" + actual + ">");
        }
    }
}
