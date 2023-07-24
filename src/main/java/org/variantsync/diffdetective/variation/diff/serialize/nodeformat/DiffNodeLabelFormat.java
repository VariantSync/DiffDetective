package org.variantsync.diffdetective.variation.diff.serialize.nodeformat;

import java.util.List;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.LineGraphConstants;
import org.variantsync.diffdetective.variation.diff.serialize.LinegraphFormat;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.functjonal.Pair;

/**
 * Reads and writes {@link DiffNode DiffNodes} from and to line graph.
 * @author Paul Bittner, Kevin Jedelhauser
 */
@FunctionalInterface
public interface DiffNodeLabelFormat<L extends Label> extends LinegraphFormat {
	/**
	 * Converts a label of line graph into a {@link DiffNode}.
	 * 
	 * @param lineGraphNodeLabel A string containing the label of the {@link DiffNode}
	 * @param nodeId The id of the {@link DiffNode}
	 * @return The corresponding {@link DiffNode}
	 */
	default DiffNode<DiffLinesLabel> fromLabelAndId(final String lineGraphNodeLabel, final int nodeId) {
	    return DiffNode.fromID(nodeId, lineGraphNodeLabel);
	}

    /**
     * Converts a {@link DiffNode} into a label suitable for exporting.
     * This may be human readable text or machine parseable metadata.
     *
     * @param node The {@link DiffNode} to be labeled
     * @return a label for {@code node}
     */
    String toLabel(DiffNode<? extends L> node);

    /**
     * Converts a {@link DiffNode} into a multi line label suitable for exporting.
     * This should be human readable text. Use a single line for machine parseable metadata
     * ({@link toLabel}).
     *
     * @param node The {@link DiffNode} to be labeled
     * @return a list of lines of the label for {@code node}
     */
    default List<String> toMultilineLabel(DiffNode<? extends L> node) {
        return List.of(toLabel(node));
    }

    /**
     * Converts a line describing a graph (starting with "t # ") in line graph format into a {@link DiffTreeSource}.
     *
     * @param lineGraphLine A line from a line graph file starting with "t #"
     * @return A pair with the first element being the id of the node specified in the given lineGrapLine.
     *         The second entry is the parsed {@link DiffNode} described by the label of this line.
     */
    default Pair<Integer, DiffNode<DiffLinesLabel>> fromLineGraphLine(final String lineGraphLine) {
        if (!lineGraphLine.startsWith(LineGraphConstants.LG_NODE)) throw new RuntimeException("Failed to parse DiffNode: Expected \"v ...\" but got \"" + lineGraphLine + "\"!"); // check if encoded DiffNode

        final int idBegin = LineGraphConstants.LG_NODE.length() + 1;
        final int idEnd = lineGraphLine.indexOf(' ', idBegin);
        final String nodeIdStr = lineGraphLine.substring(idBegin, idEnd); // extract the string between the overhead in front of the node id and the delimiter right after the node id
        final int nodeId;
        try {
            nodeId = Integer.parseInt(nodeIdStr);
        } catch (NumberFormatException e) {
            throw new RuntimeException("Given node id is not an integer: ‘" + nodeIdStr + "’");
        }

        final String label = lineGraphLine.substring(idEnd + 1);
        return new Pair<>(nodeId, fromLabelAndId(label, nodeId));
    }
}
