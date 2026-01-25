import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.function.FailableConsumer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.util.IO;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffType;
import org.variantsync.diffdetective.variation.diff.Time;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.serialize.*;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
import org.variantsync.diffdetective.variation.diff.source.CommitDiffVariationDiffSource;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.fail;

/**
 * For testing the import of a line graph.
 */
public class LineGraphTest {
    private final static LineGraphImportOptions<DiffLinesLabel> IMPORT_OPTIONS = new LineGraphImportOptions<>(
            GraphFormat.VARIATION_DIFF,
            new CommitDiffVariationDiffLabelFormat(),
            new LabelOnlyDiffNodeFormat<>(),
            new DefaultEdgeLabelFormat<>()
    );
    private final static LineGraphExportOptions<DiffLinesLabel> EXPORT_OPTIONS = new LineGraphExportOptions<>(
            IMPORT_OPTIONS
    );

    public static Stream<Path> testCases() throws IOException {
        return Files
            .list(Constants.RESOURCE_DIR.resolve("line_graph"))
            .filter(file -> file.getFileName().toString().endsWith(".lg"));
    }

    /**
     * Test the import of a line graph.
     */
    @ParameterizedTest
    @MethodSource("testCases")
    public void idempotentReadWrite(Path testFile) throws IOException {
        List<VariationDiff<DiffLinesLabel>> variationDiffs;
        try (BufferedReader lineGraph = Files.newBufferedReader(testFile)) {
            variationDiffs = LineGraphImport.fromLineGraph(lineGraph, testFile, IMPORT_OPTIONS);
        }
        assertConsistencyForAll(variationDiffs);

        assertEqualsFile(testFile, output -> LineGraphExport.toLineGraphFormat(variationDiffs, EXPORT_OPTIONS, output));
    }

    @Test
    public void testChildOrder() throws IOException, DiffParseException {
        Path expectedPath = Constants.RESOURCE_DIR.resolve("line_graph").resolve("childOrder.lg");

        // Note that this variation diff doesn't have a line diff representation because it
        // represents a move (it could be parseable using a tree differ).
        var root = DiffNode.createRoot(new DiffLinesLabel());
        var A = DiffNode.createArtifact(DiffType.NON, new DiffLineNumber(1, 1, 2), new DiffLineNumber(1, 1, 2), new DiffLinesLabel(List.of(new DiffLinesLabel.Line("A", new DiffLineNumber(1, DiffLineNumber.InvalidLineNumber, 2)))));
        var B = DiffNode.createArtifact(DiffType.NON, new DiffLineNumber(2, 2, 1), new DiffLineNumber(2, 2, 1), new DiffLinesLabel(List.of(new DiffLinesLabel.Line("B", new DiffLineNumber(2, DiffLineNumber.InvalidLineNumber, 1)))));
        root.addChild(A, Time.BEFORE);
        root.addChild(B, Time.BEFORE);
        root.addChild(B, Time.AFTER);
        root.addChild(A, Time.AFTER);

        final var variationDiff = new VariationDiff<DiffLinesLabel>(root, new CommitDiffVariationDiffSource(Path.of("fileName"), "aaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"));

        assertEqualsFile(expectedPath, output -> LineGraphExport.toLineGraphFormat(List.of(variationDiff), EXPORT_OPTIONS, output));
    }

    /**
     * Check consistency of {@link VariationDiff VariationDiffs}.
     *
     * @param treeList {@link VariationDiff} list
     */
    private static void assertConsistencyForAll(final List<VariationDiff<DiffLinesLabel>> treeList) {
//        for (final VariationDiff t : treeList) {
//            VariationDiffRenderer.WithinDiffDetective().render(t, t.getSource().toString(), Path.of("error"), PatchDiffRenderer.ErrorVariationDiffRenderOptions);
//        }
        treeList.forEach(VariationDiff::assertConsistency);
    }

    private static void assertEqualsFile(Path expectedPath, FailableConsumer<BufferedOutputStream, IOException> actualOutput) throws IOException {
        Path actualPath = expectedPath.getParent().resolve(expectedPath.getFileName().toString() + ".actual");
        try (var output = IO.newBufferedOutputStream(actualPath)) {
            actualOutput.accept(output);
        }

        try (
                var expectedFile = Files.newBufferedReader(expectedPath);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                fail("Expected the content of " + expectedPath + " but got the content of " + actualPath);
            } else {
                // Only keep output file on errors
                Files.delete(actualPath);
            }
        }
    }
}
