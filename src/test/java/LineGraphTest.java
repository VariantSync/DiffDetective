import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTree;
import diff.difftree.serialize.*;
import diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.junit.BeforeClass;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.FileUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import static org.junit.Assert.assertEquals;
/**
 * For testing the import of a line graph.
 */
public class LineGraphTest {
	private final static DiffTreeLineGraphImportOptions IMPORT_OPTIONS = new DiffTreeLineGraphImportOptions(
            GraphFormat.DIFFTREE,
            new CommitDiffDiffTreeLabelFormat(),
            new LabelOnlyDiffNodeFormat()
    );
    private final static DiffTreeLineGraphExportOptions EXPORT_OPTIONS = new DiffTreeLineGraphExportOptions(
            IMPORT_OPTIONS.graphFormat(),
            IMPORT_OPTIONS.treeFormat(),
            IMPORT_OPTIONS.nodeFormat()
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
            final String lineGraph = readLineGraphFile(testFile);
            final List<DiffTree> diffTrees = LineGraphImport.fromLineGraphFormat(lineGraph, IMPORT_OPTIONS);
            assertConsistencyForAll(diffTrees);
//            diffTrees.forEach(d -> d.forAll(n -> System.out.println(n.getLabel())));
            final String lineGraphResult = exportDiffTreeToLineGraph(diffTrees);
            assertEqualFileContent(lineGraph, lineGraphResult);
        }
	}
	
	/**
	 * Read in line graph.
	 * 
	 * @param path Relative path to the line graph
	 * @return The line graph as a string
	 */
	private static String readLineGraphFile(final Path path) {
		try {
			byte[] encoded = Files.readAllBytes(path);
			return new String(encoded, StandardCharsets.UTF_8);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
	
	/**
	 * Check consistency of {@link DiffTree DiffTrees}.
	 * 
	 * @param treeList {@link DiffTree} list
	 */
	private static void assertConsistencyForAll(final List<DiffTree> treeList) {
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
                LineGraphExport.composeTreeInLineGraph(lineGraphOutput, source, LineGraphExport.toLineGraphFormat(tree, EXPORT_OPTIONS).getValue(), EXPORT_OPTIONS);
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
		assertEquals(FileUtils.normalizedLineEndings(originalLineGraph), FileUtils.normalizedLineEndings(generatedLineGraph));
	}
	
}