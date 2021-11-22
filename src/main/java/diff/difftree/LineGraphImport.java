package diff.difftree;

import diff.CommitDiff;
import diff.difftree.serialize.DiffNodeLineGraphImporter;
import diff.difftree.serialize.DiffTreeLineGraphExporter;
import diff.difftree.serialize.IDiffNodeLineGraphImporter;
import diff.serialize.LineGraphExport;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LineGraphImport {
	
  /**
   * Reads a line graph file 
   * @return The resulting diff.
   */
  public static List<DiffTree> fromLineGraphFormat(CommitDiff diff, String lineGraph) {
	  // neue klasse: Options (enum difftree, diffGraph) (DiffTreeGraphImportOptions 
	  // nur für graphen implementieren, fromNodes verwenden
  	java.util.Scanner input = new java.util.Scanner(lineGraph);
  	
  	List<DiffTree> diffTreeList = new ArrayList<>();
  	DiffTree curDiffTree;
  	
  	HashMap<String,DiffNode> diffNodes = new HashMap<>();
  	
  	// für alle # t 
  	while (input.hasNext()) {
  		String ln = input.nextLine();
  		if (ln.startsWith("t #")) {
  			ln = ln.substring(3, ln.length() - 1);
  			String[] commit = ln.split(LineGraphExport.TREE_NAME_SEPARATOR);
  			String filePath = commit[0];
  			String commitHash = commit[1];
  			DiffNode rootDiffNode = null; // TODO woher kenne ich die Wurzel?
  			IDiffTreeSource diffTreeSource = new DiffTreeSource(filePath, commitHash);
  			curDiffTree = new DiffTree(rootDiffNode, diffTreeSource);
  			diffTreeList.add(curDiffTree);
  		} else if (ln.startsWith("v")) {
  			IDiffNodeLineGraphImporter importer = new DiffNodeLineGraphImporter(); 
  			DiffNode node = importer.parse(ln);
  		
  			// TODO wie Knoten speichern? Nötig?
  			diffNodes.put("", node);
  			
  		} else if (ln.startsWith("e")) {
  			String[] edge = ln.split(" ");
  			String fromNodeId = edge[1]; // the child node
  			String toNodeId = edge[2]; // the parent node
  			String name = edge[3];
  			DiffNode childNode = diffNodes.get(fromNodeId);
  			DiffNode parentNode = diffNodes.get(toNodeId);
  			switch (name) {
  			case DiffTreeLineGraphExporter.BEFORE_AND_AFTER_PARENT:
  				childNode.addAfterChild(parentNode);
  				childNode.addBeforeChild(parentNode);
  				break;
  			case DiffTreeLineGraphExporter.BEFORE_PARENT:
  				childNode.addBeforeChild(parentNode);
  				break;
  			case DiffTreeLineGraphExporter.AFTER_PARENT:
  				childNode.addAfterChild(parentNode);
  				break;
  			default:
  				throw new RuntimeException("Syntax error. Invalid name in edge: " + ln);
  			}
  		} else {
  			// ignore blank spaces
  		}
  	}
  	
  	input.close();
  	
  	return diffTreeList;
  }
	
}
