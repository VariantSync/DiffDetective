package diff.difftree;

import diff.difftree.serialize.DiffTreeLineGraphExporter;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import diff.difftree.serialize.DiffTreeLineGraphImportOptions.GraphFormat;
import diff.difftree.serialize.nodelabel.CodeDiffNodeLineGraphImporter;
import diff.difftree.serialize.nodelabel.DebugDiffNodeLineGraphImporter;
import diff.difftree.serialize.nodelabel.DiffTreeNodeLabelFormat;
import diff.difftree.serialize.nodelabel.LabelOnlyDiffNodeLineGraphImporter;
import diff.difftree.serialize.nodelabel.MappingsDiffNodeLineGraphImporter;
import diff.difftree.serialize.nodelabel.MiningDiffNodeLineGraphImporter;
import diff.difftree.serialize.nodelabel.TypeDiffNodeLineGraphImporter;
import diff.difftree.serialize.treelabel.DiffTreeLabelFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import util.Assert;


/**
 * Import patches from line graphs.
 */
public class LineGraphImport {
	
	/**
	 * Declaration of a {@link DiffTree} in a line graph.
	 */
	public static final String LG_TREE_HEADER = "t #";
	
	/**
	 * Declaration of a {@link DiffNode} in a line graph.
	 */
	private static final String LG_NODE = "v";
	
	/**
	 * Declaration of a connection between two {@link DiffNode DiffNodes} in a line graph.
	 */
	private static final String LG_EDGE = "e";
	
	private static DiffTreeLabelFormat treeParser;
	
	private static DiffTreeNodeLabelFormat nodeParser;
	
	/**
	 * Transforms a line graph into a list of {@link DiffTree DiffTrees}.
	 * 
	 * @return All {@link DiffTree DiffTrees} contained in the line graph
	 */
	public static List<DiffTree> fromLineGraphFormat(final String lineGraph, final DiffTreeLineGraphImportOptions options) {
		java.util.Scanner input = new java.util.Scanner(lineGraph);
		
		// All DiffTrees read from the line graph
		List<DiffTree> diffTreeList = new ArrayList<>();
		
		String previousDiffTreeLine = "";
		
		// All DiffNodes of one DiffTree for determining the root node
		List<DiffNode> diffNodeList = new ArrayList<>();
		
		// A hash map of DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer,DiffNode> diffNodes = new HashMap<>();
		
		// The currently read DiffTree with all its DiffNodes and edges
		DiffTree curDiffTree = null;
		
		// Set parser
		switch (options.style) {
		case Code:
			nodeParser = new CodeDiffNodeLineGraphImporter();
			break;
		case Debug:
			nodeParser = new DebugDiffNodeLineGraphImporter();
			break;
		case LabelOnly:
			nodeParser = new LabelOnlyDiffNodeLineGraphImporter();
			break;
		case Mappings:
			nodeParser = new MappingsDiffNodeLineGraphImporter();
			break;
		case Mining:
			nodeParser = new MiningDiffNodeLineGraphImporter();
			break;
		case Type:
			nodeParser = new TypeDiffNodeLineGraphImporter();
			break;
		}
		
		// Read the entire line graph 
		while (input.hasNext()) {
			String ln = input.nextLine();
			if (ln.startsWith(LG_TREE_HEADER)) {
				// the line represents a DiffTree
				
				if (!previousDiffTreeLine.equals("")) {
					curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
					diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
				}
					
				// set new DiffTree
				previousDiffTreeLine = ln;
				
				// Remove all DiffNodes from list
				diffNodeList.clear();
				diffNodes.clear();	
				
			} else if (ln.startsWith(LG_NODE)) {
				// the line represents a DiffNode
				
				// parse node from input line
				DiffNode node = nodeParser.readNodeFromLineGraph(ln);
				
				// add to list of current nodes
				diffNodeList.add(node);
				
				// add to hash map of current nodes
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

				if (childNode == null) throw new IllegalArgumentException(fromNodeId + " does not exits. Faulty line graph.");
				if (parentNode == null) throw new IllegalArgumentException(toNodeId + " does not exits. Faulty line graph.");
				
				switch (name) {
				// Nothing has been changed. The child-parent relationship remains the same
				case DiffTreeLineGraphExporter.BEFORE_AND_AFTER_PARENT:
					parentNode.addAfterChild(childNode);
					parentNode.addBeforeChild(childNode);
					break;
				// The child DiffNode lost its parent DiffNode (an orphan DiffNode)
				case DiffTreeLineGraphExporter.BEFORE_PARENT:
					parentNode.addBeforeChild(childNode);
					break;
				// The parent DiffNode has a new child DiffNode
				case DiffTreeLineGraphExporter.AFTER_PARENT:
					parentNode.addAfterChild(childNode);
					break;
				// A syntax error has occurred.
				default:
					// TODO custom exception
					throw new RuntimeException("Syntax error. Invalid name in edge: " + ln);
				}
			} else {
				// ignore blank spaces
				if (!ln.trim().equals("")) throw new IllegalArgumentException("Line graph contains an syntax error: " + ln);
			}
		}
		
		input.close();
		
		if (!previousDiffTreeLine.equals("")) {
			curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
			diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
		}
		
		// return all computed DiffTrees.
		return diffTreeList;
	}
	
	private static DiffTree parseDiffTree(final String lineGraph, final List<DiffNode> diffNodeList, final DiffTreeLineGraphImportOptions options) {
		// Handle trees and graphs differently
		if (options.format == GraphFormat.DIFFGRAPH) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			Assert.assertTrue(diffNodeList.stream().noneMatch(DiffNode::isRoot)); // test if it’s not a tree
			DiffTreeSource diffTreeSource = treeParser.readTreeHeaderFromLineGraph(lineGraph);
			return DiffGraph.fromNodes((Collection<DiffNode>) diffNodeList, diffTreeSource); 
		} else if (options.format == GraphFormat.DIFFTREE) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			int rootCnt = 0;
			DiffNode root = null;
			for (DiffNode v : diffNodeList) { 
				if (v.isRoot()) {
					rootCnt++;
					root = v;
				}
			}
			Assert.assertTrue(rootCnt == 1);// test if it’s a tree
			return new DiffTree(root);
		} else {
			throw new RuntimeException("Faulty GraphFormat");
		}
	}
	
}
