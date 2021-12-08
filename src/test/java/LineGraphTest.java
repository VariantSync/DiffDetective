import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTree;
import diff.difftree.serialize.*;
import diff.difftree.serialize.nodeformat.DiffTreeNodeLabelFormat;
import diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeFormat;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import org.junit.Test;

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
	
	/**
	 * Test the import of a line graph.
	 */
	@Test
	public void importLineGraphDiffTree() {
		Path filePath = Paths.get("src/test/resources/line_graph/DiffTreeTestFile.lg");
		importLineGraph(GraphFormat.DIFFTREE, filePath);
	}
	
	/**
	 * Import a line graph.
	 * 
	 * @param format {@link GraphFormat}
	 */
	private static void importLineGraph(final GraphFormat format, Path filePath) {
		String lineGraph = readLineGraphFile(filePath.toString());
		CommitDiffDiffTreeLabelFormat treeLabel = new CommitDiffDiffTreeLabelFormat();
		DiffTreeNodeLabelFormat nodeLabel = new LabelOnlyDiffNodeFormat();
		DiffTreeLineGraphImportOptions options = new DiffTreeLineGraphImportOptions(format,
				treeLabel,
				nodeLabel
				);
		List<DiffTree> diffTrees = LineGraphImport.fromLineGraphFormat(lineGraph, options);
		
		checkConsistency(diffTrees);
		
		String lineGraphResult = exportDiffTreeToLineGraph(diffTrees, format, treeLabel, nodeLabel);
		compareLineGraphs(lineGraph, lineGraphResult);
	}
	
	/**
	 * Read in line graph.
	 * 
	 * @param path Relative path to the line graph
	 * @return The line graph as a string
	 */
	private static String readLineGraphFile(final String path) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
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
	private static void checkConsistency(final List<DiffTree> treeList) {
		treeList.forEach(DiffTree::assertConsistency);
	}
	
	/**
	 * Exports computed trees to line graph.
	 * 
	 * @param treeList A list of {@link DiffTree DiffTrees}
	 * @param format {@link GraphFormat}
	 * @param treeLabel {@link CommitDiffDiffTreeLabelFormat}
	 * @param nodeLabel {@link DiffTreeNodeLabelFormat}
	 * @return The computed line graph
	 */
	private static String exportDiffTreeToLineGraph(final List<DiffTree> treeList, final GraphFormat format, final CommitDiffDiffTreeLabelFormat treeLabel, final DiffTreeNodeLabelFormat nodeLabel) {
		DiffTreeLineGraphExportOptions options = new DiffTreeLineGraphExportOptions(format, 
				treeLabel, 
				nodeLabel
				);
        final StringBuilder lineGraphOutput = new StringBuilder();
        for (var tree : treeList) {
        	if (tree.getSource() instanceof CommitDiffDiffTreeSource source) {
                LineGraphExport.composeTreeInLineGraph(lineGraphOutput, source, LineGraphExport.toLineGraphFormat(tree, options).getValue(), options);
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
	private static void compareLineGraphs(final String originalLineGraph, final String generatedLineGraph) {
		assertEquals(originalLineGraph.replace("\\r?\\n", "\\n"), generatedLineGraph.replace("\\r?\\n", "\\n"));
	}
	
}