package org.variantsync.diffdetective.diff.difftree.serialize.edgeformat;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.diff.difftree.serialize.StyledEdge;
import org.variantsync.diffdetective.diff.difftree.serialize.LinegraphFormat;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Pair;

import java.util.List;
import java.util.Map;

/**
 * Reads and writes edges between {@link DiffNode DiffNodes} from and to line graph.
 *
 * @author Kevin Jedelhauser, Paul Maximilian Bittner
 */
public abstract class EdgeLabelFormat implements LinegraphFormat {
    /**
     * Creates a new format with the {@link Direction#Default default direction}.
     */
    public EdgeLabelFormat() {
        this(Direction.Default);
    }

    /**
     * Creates a new format that uses the given direction for edge export and import.
     * @param direction The direction in which edges are written and in which read edges
     *                  are interpreted.
     */
    public EdgeLabelFormat(Direction direction) {
        setEdgeDirection(direction);
    }

    /**
     * Directions of edges.
     * Describes if a linegraph edge describes an edge from a parent to a child node
     * or vice versa.
     */
    public enum Direction {
        ChildToParent, ParentToChild;

        /**
         * Default direction is child to parent as described in our paper.
         */
        public static final Direction Default = ChildToParent;

        /**
         * Sort two values according to this direction.
         * The first argument belongs to a child node,
         * the second argument belongs to a parent node.
         * Both arguments will be ordered such that they
         * comply to this direction.
         * @param child A value related to a child node.
         * @param parent A value related to a parent node.
         * @return Both values sorted according to this direction.
         * @param <A> Value type.
         */
        public <A> Pair<A, A> sort(A child, A parent) {
            if (this == ChildToParent) {
                return new Pair<>(child, parent);
            } else {
                return new Pair<>(parent, child);
            }
        }
    }

    private Direction edgeDirection = Direction.Default;

    /**
     * Set the direction for edge IO.
     */
    public void setEdgeDirection(final Direction direction) {
        this.edgeDirection = direction;
    }

    /**
     * Returns the direction, edges in linegraph files are interpreted to have.
     */
    public Direction getEdgeDirection() {
        return edgeDirection;
    }

    /**
     * Adds the given child node as the child of the given parent node for all times described by the given label
     * (via {@link DiffNode#addBeforeChild(DiffNode)} and {@link DiffNode#addAfterChild(DiffNode)})
     * In particular, this method checks if the given edge label starts with
     * {@link LineGraphConstants#BEFORE_PARENT}, {@link LineGraphConstants#AFTER_PARENT}, or
     * {@link LineGraphConstants#BEFORE_AND_AFTER_PARENT} and connects both nodes accordingly.
     * @param child The node that should be the child of the given parent at the times described by the label.
     * @param parent The node that should be the parent of the given child at the times described by the label.
     * @param edgeLabel The label that describes the time, the edge exists at, by being prefixed with one of the
     *                  LineGraphConstants described above.
     */
    protected void connectAccordingToLabel(final DiffNode child, final DiffNode parent, final String edgeLabel) {
        if (edgeLabel.startsWith(LineGraphConstants.BEFORE_AND_AFTER_PARENT)) {
            // Nothing has been changed. The child-parent relationship remains the same
            Assert.assertTrue(parent.addAfterChild(child));
            Assert.assertTrue(parent.addBeforeChild(child));
        } else if (edgeLabel.startsWith(LineGraphConstants.BEFORE_PARENT)) {
            // The child DiffNode lost its parent DiffNode (an orphan DiffNode)
            Assert.assertTrue(parent.addBeforeChild(child));
        } else if (edgeLabel.startsWith(LineGraphConstants.AFTER_PARENT)) {
            // The parent DiffNode has a new child DiffNode
            Assert.assertTrue(parent.addAfterChild(child));
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
     * @throws IllegalArgumentException when a node referenced in the lineGraphLine does not exist in the given map.
     */
    public void connect(final String lineGraphLine, final Map<Integer, DiffNode> nodes) throws IllegalArgumentException {
        if (!lineGraphLine.startsWith(LineGraphConstants.LG_EDGE)) throw new IllegalArgumentException("Failed to parse DiffNode: Expected \"v ...\" but got \"" + lineGraphLine + "\"!"); // check if encoded DiffNode

        String[] edge = lineGraphLine.split(" ");
        String name = edge[3];

        // first is the id of the child DiffNode
        // second the id of the parent DiffNode
        final Pair<String, String> fromAndToIds = edgeDirection.sort(edge[1], edge[2]);

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
     * Converts a {@link StyledEdge} into a label suitable for exporting.
     * This may be human readable text or machine parseable metadata.
     *
     * @param edge The {@link StyledEdge} to be labeled
     * @return a label for {@code edge}
     */
    public abstract String labelOf(StyledEdge edge);

    /**
     * Converts a {@link StyledEdge} into a multi line label suitable for exporting.
     * This should be human readable text. Use a single line for machine parseable metadata
     * ({@link labelOf}).
     *
     * @param edge The {@link StyledEdge} to be labeled
     * @return a list of lines of the label for {@code edge}
     */
    public List<String> multilineLabelOf(StyledEdge edge) {
        return List.of(labelOf(edge));
    }
}
