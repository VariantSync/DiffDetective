import org.apache.commons.io.IOUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.serialize.*;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.variation.diff.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.variation.diff.serialize.treeformat.CommitDiffVariationDiffLabelFormat;
import org.variantsync.diffdetective.util.IO;

import static org.junit.jupiter.api.Assertions.fail;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Stream;

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
        return Files.list(Paths.get("src/test/resources/line_graph"));
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

        Path actualPath = testFile.getParent().resolve(testFile.getFileName().toString() + ".actual");
        try (var output = IO.newBufferedOutputStream(actualPath)) {
            LineGraphExport.toLineGraphFormat(variationDiffs, EXPORT_OPTIONS, output);
        }

        try (
                var expectedFile = Files.newBufferedReader(testFile);
                var actualFile = Files.newBufferedReader(actualPath);
        ) {
            if (!IOUtils.contentEqualsIgnoreEOL(expectedFile, actualFile)) {
                fail("The file " + testFile + " couldn't be exported or imported without modifications");
            } else {
                // Only keep output file on errors
                Files.delete(actualPath);
            }
        }
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
}
