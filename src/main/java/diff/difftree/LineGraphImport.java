package diff.difftree;

import diff.difftree.serialize.DiffTreeLineGraphImportOptions;
import diff.difftree.serialize.GraphFormat;
import diff.difftree.serialize.treeformat.DiffTreeLabelFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import util.Assert;


/**
 * Import patches from line graphs.
 */
// TODO write tests
public class LineGraphImport {
	
	/**
	 * Transforms a line graph into a list of {@link DiffTree DiffTrees}.
	 * 
	 * @return All {@link DiffTree DiffTrees} contained in the line graph
	 */
	public static List<DiffTree> fromLineGraphFormat(final String lineGraph, final DiffTreeLineGraphImportOptions options) {
		java.util.Scanner input = new java.util.Scanner(lineGraph);
		
		// All DiffTrees read from the line graph
		List<DiffTree> diffTreeList = new ArrayList<>();
		
		// All DiffNodes of one DiffTree for determining the root node
		List<DiffNode> diffNodeList = new ArrayList<>();
		
		// A hash map of DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer,DiffNode> diffNodes = new HashMap<>();

		// The currently read DiffTree with all its DiffNodes and edges
		DiffTree curDiffTree = null;
		
		// The previously read DiffTree
		String previousDiffTreeLine = "";
		
		// Read the entire line graph 
		while (input.hasNext()) {
			String ln = input.nextLine();
			if (ln.startsWith(LineGraphConstants.LG_TREE_HEADER)) {
				// the line represents a DiffTree
				
				if (!diffNodeList.isEmpty()) {
					curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
					diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
					
					// Remove all DiffNodes from list
					diffNodeList.clear();
					diffNodes.clear();	
				} 
				previousDiffTreeLine = ln;
			} else if (ln.startsWith(LineGraphConstants.LG_NODE)) {
				// the line represents a DiffNode
				
				// parse node from input line
				int nodeId = parseDiffNodeHeaderId(ln);
				String nodeLabel = parseDiffNodeHeaderLabel(ln);
				DiffNode node = options.nodeParser().readNodeFromLineGraph(nodeLabel, nodeId);
			
				// add DiffNode to lists of current DiffTree
				diffNodeList.add(node);
				diffNodes.put(node.getID(), node);
				
			} else if (ln.startsWith(LineGraphConstants.LG_EDGE)) {
				// the line represent a connection with two DiffNodes
				
				String[] edge = ln.split(" ");
				String fromNodeId = edge[1]; // the id of the child DiffNode
				String toNodeId = edge[2]; // the id of the parent DiffNode
				String name = edge[3];
				
				// Both child and parent DiffNode should exist since all DiffNodes have been read in before. Otherwise, the line graph input is faulty
				DiffNode childNode = diffNodes.get(Integer.parseInt(fromNodeId));
				DiffNode parentNode = diffNodes.get(Integer.parseInt(toNodeId));

				if (childNode == null) {
					input.close();
					throw new IllegalArgumentException(fromNodeId + " does not exits. Faulty line graph.");
				}
				if (parentNode == null) {
					input.close();
					throw new IllegalArgumentException(toNodeId + " does not exits. Faulty line graph.");
				}
				
				switch (name) {
				// Nothing has been changed. The child-parent relationship remains the same
				case LineGraphConstants.BEFORE_AND_AFTER_PARENT:
					parentNode.addAfterChild(childNode);
					parentNode.addBeforeChild(childNode);
					break;
				// The child DiffNode lost its parent DiffNode (an orphan DiffNode)
				case LineGraphConstants.BEFORE_PARENT:
					parentNode.addBeforeChild(childNode);
					break;
				// The parent DiffNode has a new child DiffNode
				case LineGraphConstants.AFTER_PARENT:
					parentNode.addAfterChild(childNode);
					break;
				// A syntax error has occurred.
				default:
					// TODO custom exception
					throw new RuntimeException("Syntax error. Invalid name in edge: " + ln);
				}
			} else {
				// ignore blank spaces
				if (!ln.trim().equals("")) {
					input.close();
					String errorMessage = String.format(
							"Line graph syntax error. Expects: \"%s\" (DiffTree), \"%s\" (DiffNode), \"%s\" (edge) or a blank space (delimiter). Faulty input: \"%s\".", 
							LineGraphConstants.LG_TREE_HEADER, 
							LineGraphConstants.LG_NODE, 
							LineGraphConstants.LG_EDGE, 
							ln);
					throw new IllegalArgumentException(errorMessage);
				}
			}
		}
		input.close();

		if (!diffNodeList.isEmpty()) {
			curDiffTree = parseDiffTree(previousDiffTreeLine, diffNodeList, options); // parse to DiffTree
			diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
		}
		
		// return all computed DiffTrees.
		return diffTreeList;
	}
	
	private static DiffTree parseDiffTree(final String lineGraph, final List<DiffNode> diffNodeList, final DiffTreeLineGraphImportOptions options) {
		// Handle trees and graphs differently
		if (options.format() == GraphFormat.DIFFGRAPH) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			Assert.assertTrue(diffNodeList.stream().noneMatch(DiffNode::isRoot)); // test if it’s not a tree
			String treeLabel = DiffTreeLabelFormat.extractRawTreeLabel(lineGraph);
			DiffTreeSource diffTreeSource = options.treeParser().readTreeHeaderFromLineGraph(treeLabel);
			return DiffGraph.fromNodes(diffNodeList, diffTreeSource); 
		} else if (options.format() == GraphFormat.DIFFTREE) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
			int rootCount = 0;
			DiffNode root = null;
			for (DiffNode v : diffNodeList) { 
				if (v.isRoot()) {
					rootCount++; 
					root = v;
				}
			}
			Assert.assertTrue(rootCount == 1);// test if it’s a tree
			return new DiffTree(root);
		} else {
			throw new RuntimeException("Unsupported GraphFormat");
		}
	}
	
	/**
	 * Returns the node id of a {@link DiffNode} in line graph.
	 * The format has to be "v $NODE_ID $LABEL".
	 * 
	 * @param lineGraphLine An entire line graph line
	 * @return The node id
	 */
	private static int parseDiffNodeHeaderId(final String lineGraphLine) {
		if (!lineGraphLine.startsWith(LineGraphConstants.LG_NODE)) throw new RuntimeException("Failed trying to parse the node id of a DiffNode: Not a DiffNode."); // check if encoded DiffNode
		return parseDiffNodeHeaderId(lineGraphLine, LineGraphConstants.LG_NODE.length() + 1);
	}
	
	/**
	 * Returns the node id of a {@link DiffNode} in line graph.
	 * The format has to be "$OVERHEAD $NODE_ID $LABEL".
	 * 
	 * @param lineGraphLine An entire line graph line
	 * @param overheadOffset The length of the overhead in front of the node id. In other words, the number of characters that need to be removed ahead
	 * @return The node id
	 */
	private static int parseDiffNodeHeaderId(final String lineGraphLine, final int overheadOffset) {
		String nodeId = lineGraphLine.substring(overheadOffset, lineGraphLine.indexOf(' ', overheadOffset)); // extract the string between the overhead in front of the node id and the delimiter right after the node id
		try {
			return Integer.parseInt(nodeId);
		} catch (NumberFormatException e) {
			throw new RuntimeException("Input cannot be parsed since the node id is not an integer: ‘" + nodeId + "’");
		}
	}
	
	/**
	 * Returns the label of a {@link DiffNode} in line graph.
	 * 
	 * @param lineGraphLine An entire line graph line
	 * @return The node label
	 */
	private static String parseDiffNodeHeaderLabel(final String lineGraphLine) {
		if (!lineGraphLine.startsWith(LineGraphConstants.LG_NODE)) throw new RuntimeException("Failed trying to parse the node id of a DiffNode: Not a DiffNode."); // check if encoded DiffNode
		return parseDiffNodeHeaderLabel(lineGraphLine, lineGraphLine.indexOf(' ', LineGraphConstants.LG_NODE.length() + 1));
	}
	
	/**
	 * Returns the label of a {@link DiffNode} in line graph.
	 * 
	 * @param lineGraphLine
	 * @param overheadOffset The length of the overhead in front of the node label. In other words, the number of characters that need to be removed ahead
	 * @return The node label
	 */
	private static String parseDiffNodeHeaderLabel(final String lineGraphLine, final int overheadOffset) {
		return lineGraphLine.substring(overheadOffset, lineGraphLine.length()); // remove overhead
	}
	
}
