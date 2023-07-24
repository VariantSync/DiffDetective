import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.serialize.*;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.LineNumberFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.fail;

public class ExportTest {
    private final static Path RESOURCE_DIR = Path.of("src/test/resources/serialize");

    /**
     * Format used for the test export.
     */
    private final static Format<DiffLinesLabel> format =
        new Format<>(
            new LineNumberFormat<>(),
            new DefaultEdgeLabelFormat<>()
        );

    /**
     * Format used to deserialize the test case.
     */
    private final static LineGraphImportOptions<DiffLinesLabel> importOptions =
        new LineGraphImportOptions<>(
            GraphFormat.VARIATION_DIFF,
            new CommitDiffVariationDiffLabelFormat(),
            new LabelOnlyDiffNodeFormat<>(),
            new DefaultEdgeLabelFormat<>()
        );

    public static boolean isGraphvizInstalled() throws InterruptedException {
        try {
            Process dotProcess = new ProcessBuilder("dot", "-V").start();
            dotProcess.waitFor();
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    @Test
    @EnabledIf("isGraphvizInstalled")
    @Disabled
    /* Paul:
     * I disabled this testcase because it is too vulnerable to randomness by graphviz.
     * Tiny changes in coordinates make the test fail although the exportex tex file is fine.
     */
    public void export() throws IOException {
        var testCasePath = RESOURCE_DIR.resolve("testcase.lg");
        var actualPath = RESOURCE_DIR.resolve("actual.tex");
        var expectedPath = RESOURCE_DIR.resolve("expected.tex");

        // Deserialize the test case.
        VariationDiff<DiffLinesLabel> variationDiff;
        try (BufferedReader lineGraph = Files.newBufferedReader(testCasePath)) {
            variationDiff = LineGraphImport.fromLineGraph(lineGraph, testCasePath, importOptions).get(0);
        }

        // Export the test case
        try (
                var unbufferedOutput = Files.newOutputStream(actualPath);
                var output = new BufferedOutputStream(unbufferedOutput)
        ) {
            new TikzExporter<>(format).exportVariationDiff(variationDiff, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                fail("The VariationDiff in file " + testCasePath + " didn't parse correctly. "
                    + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". ");
            } else {
                // Keep output file for debugging on errors
                Files.delete(actualPath);
            }
        }
    }
}
