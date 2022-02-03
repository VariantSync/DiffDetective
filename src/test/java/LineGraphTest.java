import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTree;
import diff.difftree.serialize.*;
import diff.difftree.serialize.edgeformat.DefaultEdgeLabelFormat;
import diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
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
	private final static DiffTreeLineGraphImportOptions IMPORT_OPTIONS = new DiffTreeLineGraphImportOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new LabelOnlyDiffNodeFormat(),
            new DefaultEdgeLabelFormat()
    );
    private final static DiffTreeLineGraphExportOptions EXPORT_OPTIONS = new DiffTreeLineGraphExportOptions(
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
	public void idempotentReadWrite() {
        for (final Path testFile : TEST_FILES) {
            Logger.info("Testing " + testFile);
            final String lineGraph = FileUtils.readUTF8(testFile);
            final List<DiffTree> diffTrees = LineGraphImport.fromLineGraph(lineGraph, IMPORT_OPTIONS);
            assertConsistencyForAll(diffTrees);
            final String lineGraphResult = exportDiffTreeToLineGraph(diffTrees);
            assertEqualFileContent(lineGraph, lineGraphResult);
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
	
	/**
	 * Compare two line graphs.
	 * 
	 * @param originalLineGraph The original line graph
	 * @param generatedLineGraph The generated line graph
	 */
	private static void assertEqualFileContent(final String originalLineGraph, final String generatedLineGraph) {
        final String o = FileUtils.normalizedLineEndings(originalLineGraph).trim();
        final String g = FileUtils.normalizedLineEndings(generatedLineGraph).trim();
//        System.out.println("ORIGINAL");
//        System.out.println(o);
//        System.out.println("GENERATED");
//        System.out.println(g);
        assertEquals(o, g);
	}
	
}