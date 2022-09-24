import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import org.variantsync.diffdetective.diff.difftree.CommitDiffDiffTreeSource;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.serialize.*;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import org.variantsync.diffdetective.diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.variantsync.diffdetective.util.FileUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;

import static org.junit.Assert.assertEquals;
/**
 * For testing the import of a line graph.
 */
public class LineGraphTest {
	private final static LineGraphImportOptions IMPORT_OPTIONS = new LineGraphImportOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new LabelOnlyDiffNodeFormat(),
            new DefaultEdgeLabelFormat()
    );
    private final static LineGraphExportOptions EXPORT_OPTIONS = new LineGraphExportOptions(
            IMPORT_OPTIONS
    );

    private static List<Path> TEST_FILES;

    @BeforeClass
    public static void init() throws IOException {
        TEST_FILES = Files.list(Paths.get("src/test/resources/line_graph")).toList();
    }

	/**
	 * Test the import of a line graph.
	 */
	@Test
	public void idempotentReadWrite() throws IOException {
        for (final Path testFile : TEST_FILES) {
            Logger.info("Testing {}", testFile);
            List<DiffTree> diffTrees;
            try (BufferedReader lineGraph = Files.newBufferedReader(testFile)) {
                diffTrees = LineGraphImport.fromLineGraph(lineGraph, testFile, IMPORT_OPTIONS);
            }
            assertConsistencyForAll(diffTrees);
            final String lineGraphResult = exportDiffTreeToLineGraph(diffTrees);
            TestUtils.assertEqualToFile(testFile, lineGraphResult);
        }
	}
	
	/**
	 * Check consistency of {@link DiffTree DiffTrees}.
	 * 
	 * @param treeList {@link DiffTree} list
	 */
	private static void assertConsistencyForAll(final List<DiffTree> treeList) {
//        for (final DiffTree t : treeList) {
//            DiffTreeRenderer.WithinDiffDetective().render(t, t.getSource().toString(), Path.of("error"), PatchDiffRenderer.ErrorDiffTreeRenderOptions);
//        }
		treeList.forEach(DiffTree::assertConsistency);
	}
	
	/**
	 * Exports computed trees to line graph.
	 * 
	 * @param treeList A list of {@link DiffTree DiffTrees}
	 * @return The computed line graph
	 */
	private static String exportDiffTreeToLineGraph(final List<DiffTree> treeList) {
        final StringBuilder lineGraphOutput = new StringBuilder();
        for (var tree : treeList) {
        	if (tree.getSource() instanceof CommitDiffDiffTreeSource source) {
                LineGraphExport.composeTreeInLineGraph(lineGraphOutput, source, Objects.requireNonNull(LineGraphExport.toLineGraphFormat(tree, EXPORT_OPTIONS)).second(), EXPORT_OPTIONS);
        	} else throw new RuntimeException("The DiffTreeSoruce of DiffTree " + tree + " is not a CommitDiffDiffTreeSource: " + tree.getSource());
        }
        return lineGraphOutput.toString();
	}
}
