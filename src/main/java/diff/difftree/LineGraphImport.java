package diff.difftree;

import diff.difftree.serialize.DiffTreeLineGraphExporter;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Collection;


/**
 * Import patches which are based on textual line graphs.
 */
public class LineGraphImport {
	
	/**
	 * Start of a {@link DiffTree} declaration in a line graph.
	 */
	public static final String LG_TREE_HEADER = "t #";
	
	/**
	 * Start of a {@link DiffNode} in a line graph.
	 */
	private static final String LG_NODE = "v";
	
	/**
	 * Start of a connection between two {@link DiffNode DiffNodes} line graph.
	 */
	private static final String LG_EDGE = "e";
	
	/**
	 * Reads a line graph file.
	 * 
	 * @return The all DiffTree contained in the graph file
	 */
	public static List<DiffTree> fromLineGraphFormat(final String lineGraph, final DiffTreeLineGraphImportOptions options) {
		java.util.Scanner input = new java.util.Scanner(lineGraph);
		
		// All DiffTrees read from the line graph
		List<DiffTree> diffTreeList = new ArrayList<>();
		
		// The currently read DiffTree with all its DiffNodes and edges
		// TODO needed?
		DiffTree curDiffTree;
		
		// All DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer,DiffNode> diffNodes = new HashMap<>();
		
		// Read the entire line graph 
		while (input.hasNext()) {
			String ln = input.nextLine();
			if (ln.startsWith(LG_TREE_HEADER)) {
				// the line represents a DiffTree
				
				DiffTreeSource diffTreeSource = options.treeParser.importTree(ln);

				// TODO How to create a DiffTree from diffTreeSource?
				// The DiffTree accesses all its DiffNodes by traversing starting from the root DiffNode.
				// In case no root is given, how to proceed?
				DiffNode rootDiffNode = null;
				curDiffTree = new DiffTree(rootDiffNode, diffTreeSource);
				curDiffTree = DiffGraph.fromNodes((Collection<DiffNode>) diffNodes.values(), diffTreeSource); // can also be used for DiffGraphs with one root?
				
				diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list
			} else if (ln.startsWith(LG_NODE)) {
				// the line represents a DiffNode
				
				// TODO like this?
				DiffNode node = options.nodeParser.parse(ln);
				diffNodes.put(node.getID(), node);
			} else if (ln.startsWith(LG_EDGE)) {
				// the line represent a connection with two DiffNodes
				
				String[] edge = ln.split(" ");
				String fromNodeId = edge[1]; // the id of the child DiffNode
				String toNodeId = edge[2]; // the id of the parent DiffNode
				String name = edge[3];
				
				// Both child and parent DiffNode should exist since all DiffNodes have been read in before. Otherwise, the line graph input is faulty
				DiffNode childNode = diffNodes.get(Integer.parseInt(fromNodeId));
				DiffNode parentNode = diffNodes.get(Integer.parseInt(toNodeId));
				
				switch (name) {
				// Nothing has been changed. The child-parent relationship remains the same
				case DiffTreeLineGraphExporter.BEFORE_AND_AFTER_PARENT:
					childNode.addAfterChild(parentNode);
					childNode.addBeforeChild(parentNode);
					break;
				// The child DiffNode lost its parent DiffNode (an orphan DiffNode)
				case DiffTreeLineGraphExporter.BEFORE_PARENT:
					childNode.addBeforeChild(parentNode);
					break;
				// The parent DiffNode has a new child DiffNode
				case DiffTreeLineGraphExporter.AFTER_PARENT:
					childNode.addAfterChild(parentNode);
					break;
				// A syntax error has occurred.
				default:
					throw new RuntimeException("Syntax error. Invalid name in edge: " + ln);
				}
			} else {
				// ignore blank spaces
			}
		}
		
		input.close();
		
		// return all computed DiffTrees.
		return diffTreeList;
	}
	
}
