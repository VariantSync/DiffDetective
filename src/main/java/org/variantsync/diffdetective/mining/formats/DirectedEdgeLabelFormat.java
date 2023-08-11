package org.variantsync.diffdetective.mining.formats;

import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.serialize.StyledEdge;
import org.variantsync.diffdetective.variation.diff.serialize.edgeformat.EdgeLabelFormat;
import org.variantsync.functjonal.Functjonal;

public final class DirectedEdgeLabelFormat extends EdgeLabelFormat<DiffLinesLabel> {
    private final static String LABEL_SEPARATOR = ">";
    private record Edge<L extends DiffLinesLabel>(DiffNode<L> from, DiffNode<L> to) {}

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

    private <L extends DiffLinesLabel> Edge<L> recoverEdgeDirectionFromLabelIfPossible(DiffNode<L> hypothesizedFrom, DiffNode<L> hypothesizedTo, String edgeLabel) {
        final String[] labelParts = edgeLabel.split(LABEL_SEPARATOR);
        final String fromLabel = labelParts[1];
        final String toLabel = labelParts[2];

        final String hypothesizedFromLabel = nodeFormatter.toLabel(hypothesizedFrom);
        final String hypothesizedToLabel = nodeFormatter.toLabel(hypothesizedTo);

        if (fromLabel.equals(hypothesizedFromLabel) || toLabel.equals(hypothesizedToLabel)) {
            return new Edge<>(hypothesizedFrom, hypothesizedTo);
        } else if (toLabel.equals(hypothesizedFromLabel) || fromLabel.equals(hypothesizedToLabel)) {
            return new Edge<>(hypothesizedTo, hypothesizedFrom);
        }

        // Can't resolve direction from labels so just assume the hypothesis was right.
        return new Edge<>(hypothesizedFrom, hypothesizedTo);
    }

    @Override
    protected <L extends DiffLinesLabel> void connectAccordingToLabel(DiffNode<L> child, DiffNode<L> parent, String edgeLabel) {
        if (useDirectionHeuristic) {
            final Edge<L> e = recoverEdgeDirectionFromLabelIfPossible(child, parent, edgeLabel);
            child = e.from;
            parent = e.to;
        }

        super.connectAccordingToLabel(child, parent, edgeLabel);
    }

    @Override
    public <L extends DiffLinesLabel> String labelOf(StyledEdge<L> edge) {
        return Functjonal.intercalate(
            LABEL_SEPARATOR,
            "",
            nodeFormatter.toLabel(edge.from()),
            nodeFormatter.toLabel(edge.to())
        );
    }

    @Override
    public String getIdentifier() {
        return this.getClass().getName() + " with " + nodeFormatter.getIdentifier();
    }
}
