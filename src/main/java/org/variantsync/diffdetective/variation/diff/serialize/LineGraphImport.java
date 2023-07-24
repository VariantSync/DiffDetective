package org.variantsync.diffdetective.variation.diff.serialize;

import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.util.FileUtils;
import org.variantsync.diffdetective.variation.diff.DiffGraph;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.source.LineGraphFileSource;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.NodeType;
import org.variantsync.functjonal.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


/**
 * Import VariationDiffs from line graph files.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public class LineGraphImport {
    /**
     * Import all VariationDiffs from the given linegraph file.
     *
	 * @param path Path to a linegraph file in which only VariationDiffs are stored.
	 * @param options Options for the import, such as hints for the used formats for node and edge labels.
     * @return All {@link VariationDiff VariationDiffs} contained in the linegraph file.
	 * @throws IOException when {@link LineGraphImport#fromLineGraph(BufferedReader, Path, LineGraphImportOptions)} throws.
     */
    public static List<VariationDiff<DiffLinesLabel>> fromFile(final Path path, final LineGraphImportOptions<DiffLinesLabel> options) throws IOException {
        Assert.assertTrue(Files.isRegularFile(path));
        Assert.assertTrue(FileUtils.isLineGraph(path));
        try (BufferedReader input = Files.newBufferedReader(path)) {
            return fromLineGraph(input, path, options);
        }
    }
	
	/**
	 * Import all VariationDiffs from the given linegraph file.
	 *
	 * @param lineGraph Reader that reads the linegraph file.
	 * @param originalFile Path to the file from which the lineGraph reader is reading.
	 * @param options Options for the import, such as hints for the used formats for node and edge labels.
	 * @return All {@link VariationDiff VariationDiffs} contained in the linegraph text.
	 */
	public static List<VariationDiff<DiffLinesLabel>> fromLineGraph(final BufferedReader lineGraph, final Path originalFile, final LineGraphImportOptions<DiffLinesLabel> options) throws IOException {
		// All VariationDiffs read from the line graph
		List<VariationDiff<DiffLinesLabel>> variationDiffList = new ArrayList<>();

		// All DiffNodes of one VariationDiff for determining the root node
		List<DiffNode<DiffLinesLabel>> diffNodeList = new ArrayList<>();

		// A hash map of DiffNodes
		// <id of DiffNode, DiffNode>
		HashMap<Integer, DiffNode<DiffLinesLabel>> diffNodes = new HashMap<>();

		// The previously read VariationDiff
		String previousVariationDiffLine = "";
		
		// Read the entire line graph 
		String ln;
		while ((ln = lineGraph.readLine()) != null) {
			if (ln.startsWith(LineGraphConstants.LG_TREE_HEADER)) {
				// the line represents a VariationDiff
				
				if (!diffNodeList.isEmpty()) {
					VariationDiff<DiffLinesLabel> curVariationDiff = parseVariationDiff(previousVariationDiffLine, originalFile, diffNodeList, options); // parse to VariationDiff
					variationDiffList.add(curVariationDiff); // add newly computed VariationDiff to the list of all VariationDiffs
					
					// Remove all DiffNodes from list
					diffNodeList.clear();
					diffNodes.clear();	
				} 
				previousVariationDiffLine = ln;
			} else if (ln.startsWith(LineGraphConstants.LG_NODE)) {
				// the line represents a DiffNode
				
				// parse node from input line
				final Pair<Integer, DiffNode<DiffLinesLabel>> idAndNode = options.nodeFormat().fromLineGraphLine(ln);
			
				// add DiffNode to lists of current VariationDiff
				diffNodeList.add(idAndNode.second());
				diffNodes.put(idAndNode.first(), idAndNode.second());
				
			} else if (ln.startsWith(LineGraphConstants.LG_EDGE)) {
				// the line represent a connection with two DiffNodes
				options.edgeFormat().connect(ln, diffNodes);
			} else if (!ln.isBlank()) {
				// ignore blank lines and throw an exception otherwise
				String errorMessage = String.format(
						"Line graph syntax error. Expects: \"%s\" (VariationDiff), \"%s\" (DiffNode), \"%s\" (edge) or a blank space (delimiter). Faulty input: \"%s\".", 
						LineGraphConstants.LG_TREE_HEADER,
						LineGraphConstants.LG_NODE,
						LineGraphConstants.LG_EDGE,
						ln);
				throw new IllegalArgumentException(errorMessage);
			}
		}

		if (!diffNodeList.isEmpty()) {
			VariationDiff<DiffLinesLabel> curVariationDiff = parseVariationDiff(previousVariationDiffLine, originalFile, diffNodeList, options); // parse to VariationDiff
			variationDiffList.add(curVariationDiff); // add newly computed VariationDiff to the list of all VariationDiffs
		}
		
		// return all computed VariationDiffs.
		return variationDiffList;
	}
	
	/**
	 * Generates a {@link VariationDiff} from the given, already parsed parameters.
	 * 
	 * @param lineGraph The header line in the linegraph that describes the VariationDiff (starting with <code>t #</code>).
	 * @param inFile Path to the linegraph file that is currently parsed.
	 * @param diffNodeList All nodes of the VariationDiff that is to be created. The nodes can be assumed to be complete and already connected.
	 * @param options {@link LineGraphImportOptions}
	 * @return {@link VariationDiff} generated from the given, already parsed parameters.
	 */
	private static VariationDiff<DiffLinesLabel> parseVariationDiff(final String lineGraph, final Path inFile, final List<DiffNode<DiffLinesLabel>> diffNodeList, final LineGraphImportOptions<DiffLinesLabel> options) {
		VariationDiffSource variationDiffSource = options.treeFormat().fromLineGraphLine(lineGraph);

		if (variationDiffSource == null || VariationDiffSource.Unknown.equals(variationDiffSource)) {
			variationDiffSource = new LineGraphFileSource(
					lineGraph,
					inFile
			);
		}

		// Handle trees and graphs differently
		if (options.graphFormat() == GraphFormat.DIFFGRAPH) {
			return DiffGraph.fromNodes(diffNodeList, variationDiffSource);
		} else if (options.graphFormat() == GraphFormat.VARIATION_DIFF) {
			// If you should interpret the input data as VariationDiffs, always expect a root to be present. Parse all nodes (v) to a list of nodes. Search for the root. Assert that there is exactly one root.
            DiffNode<DiffLinesLabel> root = null;
            for (final DiffNode<DiffLinesLabel> v : diffNodeList) {
                if (v.isRoot()) {
                    // v is root candidate
                    if (root != null) {
                        throw new RuntimeException("Not a VariationDiff: Got more than one root! Got \"" + root + "\" and \"" + v + "\"!");
                    }
                    if (v.getNodeType() == NodeType.IF) {
                        root = v;
                    } else {
                        throw new RuntimeException("Not a VariationDiff but a DiffGraph: The node \"" + v + "\" is not labeled as IF but has no parents!");
                    }
                }
            }

            if (root == null) {
                throw new RuntimeException("Not a VariationDiff but a DiffGraph: No root found!");
            }

//            countRootTypes.merge(root.getNodeType(), 1, Integer::sum);

			return new VariationDiff<>(root, variationDiffSource);
		} else {
			throw new RuntimeException("Unsupported GraphFormat");
		}
	}
}
