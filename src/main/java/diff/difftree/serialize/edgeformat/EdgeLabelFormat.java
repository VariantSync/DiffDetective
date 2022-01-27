package diff.difftree.serialize.edgeformat;

import de.variantsync.functjonal.Product;
import diff.difftree.DiffNode;
import diff.difftree.LineGraphConstants;
import diff.difftree.serialize.LinegraphFormat;
import util.StringUtils;

import java.util.Map;

/**
 * Reads and writes edges between {@link DiffNode DiffNodes} from and to line graph.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public abstract class EdgeLabelFormat implements LinegraphFormat {
    public EdgeLabelFormat() {
        this(Direction.Default);
    }

    public EdgeLabelFormat(Direction direction) {
        setEdgeDirection(direction);
    }

    public enum Direction {
        ChildToParent, ParentToChild;

        public static final Direction Default = ChildToParent;

        <A> Product<A, A> sort(A child, A parent) {
            if (this == ChildToParent) {
                return new Product<>(child, parent);
            } else {
                return new Product<>(parent, child);
            }
        }
    }

    private Direction edgeDirection = Direction.Default;

    public void setEdgeDirection(final Direction direction) {
        this.edgeDirection = direction;
    }

    public Direction getEdgeDirection() {
        return edgeDirection;
    }

    protected void connectAccordingToLabel(final DiffNode child, final DiffNode parent, final String edgeLabel) {
        if (edgeLabel.startsWith(LineGraphConstants.BEFORE_AND_AFTER_PARENT)) {
            // Nothing has been changed. The child-parent relationship remains the same
            child.addAfterChild(child);
            parent.addBeforeChild(child);
        } else if (edgeLabel.startsWith(LineGraphConstants.BEFORE_PARENT)) {
            // The child DiffNode lost its parent DiffNode (an orphan DiffNode)
            parent.addBeforeChild(child);
        } else if (edgeLabel.startsWith(LineGraphConstants.AFTER_PARENT)) {
            // The parent DiffNode has a new child DiffNode
            parent.addAfterChild(child);
        } else {
            throw new IllegalArgumentException("Syntax error. Invalid name in edge label " + edgeLabel);
        }
    }

    /**
     * Parses the edge in the given lineGraphLine.
     * Connects the two nodes referenced by the edge accordingly.
     * Assumes that both nodes being referenced in the parsed line exist in the given collection.
     *
     * @param lineGraphLine A line from a line graph file describing an edge.
     * @param nodes All nodes that have been parsed so far, indexed by their id.
     */
    public void connect(final String lineGraphLine, final Map<Integer, DiffNode> nodes) throws IllegalArgumentException {
        if (!lineGraphLine.startsWith(LineGraphConstants.LG_EDGE)) throw new IllegalArgumentException("Failed to parse DiffNode: Expected \"v ...\" but got \"" + lineGraphLine + "\"!"); // check if encoded DiffNode

        String[] edge = lineGraphLine.split(" ");
        String name = edge[3];

        // first is the id of the child DiffNode
        // second the id of the parent DiffNode
        final Product<String, String> fromAndToIds = edgeDirection.sort(edge[1], edge[2]);

        // Both child and parent DiffNode should exist since all DiffNodes have been read in before. Otherwise, the line graph input is faulty
        DiffNode childNode = nodes.get(Integer.parseInt(fromAndToIds.first()));
        DiffNode parentNode = nodes.get(Integer.parseInt(fromAndToIds.second()));

        if (childNode == null) {
            throw new IllegalArgumentException(fromAndToIds.first() + " does not exits. Faulty line graph.");
        }
        if (parentNode == null) {
            throw new IllegalArgumentException(fromAndToIds.second() + " does not exits. Faulty line graph.");
        }

        connectAccordingToLabel(childNode, parentNode, name);
    }

    /**
     * Serializes the edges from given node to its parent
     * to a string of lines, where each edge is placed on one line.
     *
     * @param node The {@link DiffNode} whose edges to parents to export.
     * @return Linegraph lines for each edge from the given node to its parents. All lines are put into the same string and separated by a line break ("\n").
     */
    public String getParentEdgeLines(final DiffNode node) {
        final DiffNode beforeParent = node.getBeforeParent();
        final DiffNode afterParent = node.getAfterParent();
        final boolean hasBeforeParent = beforeParent != null;
        final boolean hasAfterParent = afterParent != null;

        StringBuilder edgesString = new StringBuilder();
        // If the node has exactly one parent
        if (hasBeforeParent && hasAfterParent && beforeParent == afterParent) {
            edgesString
                    .append(edgeToLineGraphSorted(node, beforeParent, LineGraphConstants.BEFORE_AND_AFTER_PARENT))
                    .append(StringUtils.LINEBREAK);
        } else {
            if (hasBeforeParent) {
                edgesString
                        .append(edgeToLineGraphSorted(node, beforeParent, LineGraphConstants.BEFORE_PARENT))
                        .append(StringUtils.LINEBREAK);
            }
            if (hasAfterParent) {
                edgesString
                        .append(edgeToLineGraphSorted(node, afterParent, LineGraphConstants.AFTER_PARENT))
                        .append(StringUtils.LINEBREAK);
            }
        }
        return edgesString.toString();
    }

    private String edgeToLineGraphSorted(DiffNode desiredFrom, DiffNode desiredTo, final String labelPrefix) {
        final Product<DiffNode, DiffNode> sorted = edgeDirection.sort(desiredFrom, desiredTo);
        return edgeToLineGraph(sorted.first(), sorted.second(), labelPrefix);
    }

    protected abstract String edgeToLineGraph(DiffNode from, DiffNode to, final String labelPrefix);
}
