package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.serialize.StyledEdge;
import org.variantsync.diffdetective.diff.difftree.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.functjonal.Functjonal;

public final class DirectedEdgeLabelFormat extends EdgeLabelFormat {
    private final static String LABEL_SEPARATOR = ">";
    private record Edge(DiffNode from, DiffNode to) {}

    private final MiningNodeFormat nodeFormatter;
    private final boolean useDirectionHeuristic;

    public DirectedEdgeLabelFormat(MiningNodeFormat nodeFormatter) {
        this(nodeFormatter, true, Direction.Default);
    }

    public DirectedEdgeLabelFormat(MiningNodeFormat nodeFormatter, boolean useDirectionHeuristic, EdgeLabelFormat.Direction direction) {
        super(direction);
        this.nodeFormatter = nodeFormatter;
        this.useDirectionHeuristic = useDirectionHeuristic;
    }

    private Edge recoverEdgeDirectionFromLabelIfPossible(DiffNode hypothesizedFrom, DiffNode hypothesizedTo, String edgeLabel) {
        final String[] labelParts = edgeLabel.split(LABEL_SEPARATOR);
        final String fromLabel = labelParts[1];
        final String toLabel = labelParts[2];

        final String hypothesizedFromLabel = nodeFormatter.toLabel(hypothesizedFrom);
        final String hypothesizedToLabel = nodeFormatter.toLabel(hypothesizedTo);

        if (fromLabel.equals(hypothesizedFromLabel) || toLabel.equals(hypothesizedToLabel)) {
            return new Edge(hypothesizedFrom, hypothesizedTo);
        } else if (toLabel.equals(hypothesizedFromLabel) || fromLabel.equals(hypothesizedToLabel)) {
            return new Edge(hypothesizedTo, hypothesizedFrom);
        }

        // Can't resolve direction from labels so just assume the hypothesis was right.
        return new Edge(hypothesizedFrom, hypothesizedTo);
    }

    @Override
    protected void connectAccordingToLabel(DiffNode child, DiffNode parent, String edgeLabel) {
        if (useDirectionHeuristic) {
            final Edge e = recoverEdgeDirectionFromLabelIfPossible(child, parent, edgeLabel);
            child = e.from;
            parent = e.to;
        }

        super.connectAccordingToLabel(child, parent, edgeLabel);
    }

    @Override
    public String labelOf(StyledEdge edge) {
        return Functjonal.intercalate(
            LABEL_SEPARATOR,
            "",
            nodeFormatter.toLabel(edge.from()),
            nodeFormatter.toLabel(edge.to())
        );
    }

    @Override
    public String getName() {
        return this.getClass().getName() + " with " + nodeFormatter.getName();
    }
}
