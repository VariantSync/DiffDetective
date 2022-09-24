package org.variantsync.diffdetective.diff.difftree.serialize;

import org.variantsync.diffdetective.diff.difftree.*;
import org.variantsync.diffdetective.diff.difftree.source.LineGraphFileSource;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.functjonal.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Import DiffTrees from line graph files.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class LineGraphImport {
    /**
     * Import all DiffTrees from the given linegraph file.
     *
	 * @param path Path to a linegraph file in which only DiffTrees are stored.
	 * @param options Options for the import, such as hints for the used formats for node and edge labels.
     * @return All {@link DiffTree DiffTrees} contained in the linegraph file.
	 * @throws IOException when {@link LineGraphImport#fromLineGraph(BufferedReader, Path, LineGraphImportOptions)} throws.
     */
    public static List<DiffTree> fromFile(final Path path, final LineGraphImportOptions options) throws IOException {
        Assert.assertTrue(Files.isRegularFile(path));
        Assert.assertTrue(FileUtils.isLineGraph(path));
        try (BufferedReader input = Files.newBufferedReader(path)) {
            return fromLineGraph(input, path, options);
        }
    }
	
	/**
	 * Import all DiffTrees from the given linegraph file.
	 *
	 * @param lineGraph Reader that reads the linegraph file.
	 * @param originalFile Path to the file from which the lineGraph reader is reading.
	 * @param options Options for the import, such as hints for the used formats for node and edge labels.
	 * @return All {@link DiffTree DiffTrees} contained in the linegraph text.
	 */
	public static List<DiffTree> fromLineGraph(final BufferedReader lineGraph, final Path originalFile, final LineGraphImportOptions options) throws IOException {
		// All DiffTrees read from the line graph
		List<DiffTree> diffTreeList = new ArrayList<>();
		
		// All DiffNodes of one DiffTree for determining the root node
		List<DiffNode> diffNodeList = new ArrayList<>();
		
		// A hash map of DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer, DiffNode> diffNodes = new HashMap<>();

		// The previously read DiffTree
		String previousDiffTreeLine = "";
		
		// Read the entire line graph 
		String ln;
		while ((ln = lineGraph.readLine()) != null) {
			if (ln.startsWith(LineGraphConstants.LG_TREE_HEADER)) {
				// the line represents a DiffTree
				
				if (!diffNodeList.isEmpty()) {
					DiffTree curDiffTree = parseDiffTree(previousDiffTreeLine, originalFile, diffNodeList, options); // parse to DiffTree
					diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
					
					// Remove all DiffNodes from list
					diffNodeList.clear();
					diffNodes.clear();	
				} 
				previousDiffTreeLine = ln;
			} else if (ln.startsWith(LineGraphConstants.LG_NODE)) {
				// the line represents a DiffNode
				
				// parse node from input line
				final Pair<Integer, DiffNode> idAndNode = options.nodeFormat().fromLineGraphLine(ln);
			
				// add DiffNode to lists of current DiffTree
				diffNodeList.add(idAndNode.second());
				diffNodes.put(idAndNode.first(), idAndNode.second());
				
			} else if (ln.startsWith(LineGraphConstants.LG_EDGE)) {
				// the line represent a connection with two DiffNodes
				options.edgeFormat().connect(ln, diffNodes);
			} else if (!ln.isBlank()) {
				// ignore blank lines and throw an exception otherwise
				String errorMessage = String.format(
						"Line graph syntax error. Expects: \"%s\" (DiffTree), \"%s\" (DiffNode), \"%s\" (edge) or a blank space (delimiter). Faulty input: \"%s\".", 
						LineGraphConstants.LG_TREE_HEADER,
						LineGraphConstants.LG_NODE,
						LineGraphConstants.LG_EDGE,
						ln);
				throw new IllegalArgumentException(errorMessage);
			}
		}

		if (!diffNodeList.isEmpty()) {
			DiffTree curDiffTree = parseDiffTree(previousDiffTreeLine, originalFile, diffNodeList, options); // parse to DiffTree
			diffTreeList.add(curDiffTree); // add newly computed DiffTree to the list of all DiffTrees
		}
		
		// return all computed DiffTrees.
		return diffTreeList;
	}
	
	/**
	 * Generates a {@link DiffTree} from the given, already parsed parameters.
	 * 
	 * @param lineGraph The header line in the linegraph that describes the DiffTree (starting with <code>t #</code>).
	 * @param inFile Path to the linegraph file that is currently parsed.
	 * @param diffNodeList All nodes of the DiffTree that is to be created. The nodes can be assumed to be complete and already connected.
	 * @param options {@link LineGraphImportOptions}
	 * @return {@link DiffTree} generated from the given, already parsed parameters.
	 */
	private static DiffTree parseDiffTree(final String lineGraph, final Path inFile, final List<DiffNode> diffNodeList, final LineGraphImportOptions options) {
		DiffTreeSource diffTreeSource = options.treeFormat().fromLineGraphLine(lineGraph);

		if (diffTreeSource == null || DiffTreeSource.Unknown.equals(diffTreeSource)) {
			diffTreeSource = new LineGraphFileSource(
					lineGraph,
					inFile
			);
		}

		// Handle trees and graphs differently
		if (options.graphFormat() == GraphFormat.DIFFGRAPH) {
			return DiffGraph.fromNodes(diffNodeList, diffTreeSource);
		} else if (options.graphFormat() == GraphFormat.DIFFTREE) {
			// If you should interpret the input data as DiffTrees, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
            DiffNode root = null;
            for (final DiffNode v : diffNodeList) {
                if (v.isRoot()) {
                    // v is root candidate
                    if (root != null) {
                        throw new RuntimeException("Not a DiffTree: Got more than one root! Got \"" + root + "\" and \"" + v + "\"!");
                    }
                    if (v.nodeType == NodeType.IF) {
                        root = v;
                    } else {
                        throw new RuntimeException("Not a DiffTree but a DiffGraph: The node \"" + v + "\" is not labeled as IF but has no parents!");
                    }
                }
            }

            if (root == null) {
                throw new RuntimeException("Not a DiffTree but a DiffGraph: No root found!");
            }

//            countRootTypes.merge(root.nodeType, 1, Integer::sum);

			return new DiffTree(root, diffTreeSource);
		} else {
			throw new RuntimeException("Unsupported GraphFormat");
		}
	}
}
