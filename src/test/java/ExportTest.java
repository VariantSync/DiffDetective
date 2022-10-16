import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.*;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.LineNumberFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.Format;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class ExportTest {
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
        // Deserialize the test case.
        var testFile = Path.of("src/test/resources/serialize/testcase.lg");
        DiffTree diffTree;
        try (BufferedReader lineGraph = Files.newBufferedReader(testFile)) {
            diffTree = LineGraphImport.fromLineGraph(lineGraph, testFile, importOptions).get(0);
        }

        // Export the test case
        var tikzOutput = new ByteArrayOutputStream();
        new TikzExporter(format).exportDiffTree(diffTree, tikzOutput);

        TestUtils.assertEqualToFile(Path.of("src/test/resources/serialize/expected.tex"), tikzOutput.toString());
    }
}
