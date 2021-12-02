import diff.difftree.CommitDiffDiffTreeSource;
import diff.difftree.DiffTree;
import diff.difftree.LineGraphImport;
import diff.difftree.serialize.DiffTreeLineGraphExportOptions;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.LabelOnlyDiffNodeLineGraphImporter;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;
import diff.serialize.LineGraphExport;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import org.junit.Test;
import org.pmw.tinylog.Logger;
import util.IO;
import util.StringUtils;

/**
 * For testing the import of a line graph.
 */
public class LineGraphTest {
	
	/**
	 * Test the import of a line graph.
	 */
	@Test
	public void importLineGraphDiffTree() {
		String filePath = "linegraph/data/DiffTreeTestFile.lg";
		importLineGraph(GraphFormat.DIFFTREE, filePath);
	}
	
	/**
	 * Import a line graph.
	 * 
	 * @param format {@link GraphFormat}
	 */
	private static void importLineGraph(final GraphFormat format, String filePath) {
		String lineGraph = readLineGraphFile(filePath);
		CommitDiffDiffTreeLabelFormat treeLabel = new CommitDiffDiffTreeLabelFormat();
		LabelOnlyDiffNodeLineGraphImporter nodeLabel = new LabelOnlyDiffNodeLineGraphImporter();
		DiffTreeLineGraphImportOptions options = new DiffTreeLineGraphImportOptions(format,
				treeLabel,
				nodeLabel
				);
		List<DiffTree> diffTrees = LineGraphImport.fromLineGraphFormat(lineGraph, options);
		checkConsistency(diffTrees);
		compareDiffTrees(diffTrees, filePath, format, treeLabel, nodeLabel);
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
		treeList.stream().forEachOrdered(t -> t.assertConsistency());
	}
	
	private static void compareDiffTrees(List<DiffTree> treeList, String inputFile, GraphFormat format, CommitDiffDiffTreeLabelFormat treeLabel, LabelOnlyDiffNodeLineGraphImporter nodeLabel) {
		DiffTreeLineGraphExportOptions options = new DiffTreeLineGraphExportOptions(format, 
				new CommitDiffDiffTreeLabelFormat(), 
				new LabelOnlyDiffNodeLineGraphImporter()
				);
        final StringBuilder lineGraphOutput = new StringBuilder();

        for (var tree : treeList) {
        	CommitDiffDiffTreeSource source = (CommitDiffDiffTreeSource) tree.getSource();
        	
        	lineGraphOutput
				.append(DiffTreeLabelFormat.setRawTreeLabel(options.treeParser().writeTreeHeaderToLineGraph(source))) // print "t # $LABEL"
				.append(StringUtils.LINEBREAK)
				.append(LineGraphExport.toLineGraphFormat(tree, options).getValue())
				.append(StringUtils.LINEBREAK)
				.append(StringUtils.LINEBREAK);
        }
        
        try {
            Logger.info("Writing file " + inputFile + ".out");
            IO.write(Paths.get(inputFile + ".out"), lineGraphOutput.toString());
        } catch (IOException exception) {
            Logger.error(exception);
        }

	}
}
