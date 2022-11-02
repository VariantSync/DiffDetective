import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.*;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.LineNumberFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.Format;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportTest {
    private final static Path RESOURCE_DIR = Path.of("src/test/resources/serialize");

    /**
     * Format used for the test export.
     */
    private final static Format format =
        new Format(
            new LineNumberFormat(),
            new DefaultEdgeLabelFormat()
        );

    /**
     * Format used to deserialize the test case.
     */
    private final static LineGraphImportOptions importOptions =
        new LineGraphImportOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new LabelOnlyDiffNodeFormat(),
            new DefaultEdgeLabelFormat()
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
    public void export() throws IOException {
        var testCasePath = RESOURCE_DIR.resolve("testcase.lg");
        var actualPath = RESOURCE_DIR.resolve("actual.tex");
        var expectedPath = RESOURCE_DIR.resolve("expected.tex");

        // Deserialize the test case.
        DiffTree diffTree;
        try (BufferedReader lineGraph = Files.newBufferedReader(testCasePath)) {
            diffTree = LineGraphImport.fromLineGraph(lineGraph, testCasePath, importOptions).get(0);
        }

        // Export the test case
        try (
                var unbufferedOutput = Files.newOutputStream(actualPath);
                var output = new BufferedOutputStream(unbufferedOutput)
        ) {
            new TikzExporter(format).exportDiffTree(diffTree, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                fail("The DiffTree in file " + testCasePath + " didn't parse correctly. "
                    + "Expected the content of " + expectedPath + " but got the content of " + actualPath + ". ");
            } else {
                // Keep output file for debugging on errors
                Files.delete(actualPath);
            }
        }
    }
}
