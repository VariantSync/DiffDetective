import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.LineGraphImport;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.nodeformat.CodeDiffNodeLineGraphImporter;
import diff.difftree.serialize.treeformat.CommitDiffDiffTreeLabelFormat;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.Test;

public class LineGraphTest {

	@Test
	public void importLineGraphDiffGraph() {
		importLineGraph(GraphFormat.DIFFGRAPH);
	}
	
	@Test
	public void importLineGraphDiffTree() {
		importLineGraph(GraphFormat.DIFFTREE);
	}
	
	@Test
	public void nodeIdTest() {
		for (int i = 0; i < 100; ++i) {
			int failId = ThreadLocalRandom.current().nextInt(0, 1000 + 1);
			DiffNode.fromID(failId); // crash
		}
	}
	
	private static void importLineGraph(GraphFormat format) {
		
		String lineGraph = readLineGraphFile("linegraph/data/difftrees.lg", StandardCharsets.UTF_8);
		DiffTreeLineGraphImportOptions options = new DiffTreeLineGraphImportOptions(format,
				new CommitDiffDiffTreeLabelFormat(),
				new CodeDiffNodeLineGraphImporter()
				);
		List<DiffTree> diffTrees = LineGraphImport.fromLineGraphFormat(lineGraph, options);
		
	}
	
	private static String readLineGraphFile(String path, Charset encoding) {
		try {
			byte[] encoded = Files.readAllBytes(Paths.get(path));
			return new String(encoded, encoding);
		} catch (IOException e) {
			e.printStackTrace();
			return "";
		}
	}
}
