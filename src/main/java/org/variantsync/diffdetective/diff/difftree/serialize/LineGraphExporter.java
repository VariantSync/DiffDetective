package org.variantsync.diffdetective.diff.difftree.serialize;

import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;

import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.LineGraphConstants;
import org.variantsync.diffdetective.relationshipedges.*;
import org.variantsync.diffdetective.util.StringUtils;
import org.variantsync.functjonal.Functjonal;

/**
 * Exporter that converts a single DiffTree's nodes and edges to linegraph.
 */
public class LineGraphExporter implements Exporter {
    private final Format format;
    private final DiffTreeSerializeDebugData debugData;

    public LineGraphExporter(Format format) {
        this.format = format;
        this.debugData = new DiffTreeSerializeDebugData();
    }

    public LineGraphExporter(LineGraphExportOptions options) {
        this(new Format(options.nodeFormat(), options.edgeFormat()));
    }

    /**
     * Export a line graph of {@code diffTree} into {@code destination}.
     *
     * @param diffTree to be exported
     * @param destination where the result should be written
     */
    @Override
    public void exportDiffTree(DiffTree diffTree, OutputStream destination) {
        var output = new PrintStream(destination);
        format.forEachNode(diffTree, (node) -> {
            switch (node.diffType) {
                case ADD -> ++debugData.numExportedAddNodes;
                case REM -> ++debugData.numExportedRemNodes;
                case NON -> ++debugData.numExportedNonNodes;
            }

            output.println(LineGraphConstants.LG_NODE + " " + node.getID() + " " + format.getNodeFormat().toLabel(node));
        });

        format.forEachUniqueEdge(diffTree, (edges) -> {
            output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edges.get(0).from().getID(), edges.get(0).to().getID(), ""));

            for (var edge : edges) {
                output.print(edge.style().lineGraphType());
            }

            for (var edge : edges) {
                output.print(format.getEdgeFormat().labelOf(edge));
            }

            output.println();
        });
    }

    public void exportDiffTree(EdgeTypedDiff edgeTypedDiff, OutputStream destination) {
        var output = new PrintStream(destination);
        format.forEachNode(edgeTypedDiff.getDiffTree(), (node) -> {
            switch (node.diffType) {
                case ADD -> ++debugData.numExportedAddNodes;
                case REM -> ++debugData.numExportedRemNodes;
                case NON -> ++debugData.numExportedNonNodes;
            }

            output.println(LineGraphConstants.LG_NODE + " " + node.getID() + " " + format.getNodeFormat().toLabel(node));
        });

        format.forEachUniqueEdge(edgeTypedDiff.getDiffTree(), (edges) -> {
            output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edges.get(0).from().getID(), edges.get(0).to().getID(), ""));

            for (var edge : edges) {
                output.print(edge.style().lineGraphType());
            }

            for (var edge : edges) {
                output.print(format.getEdgeFormat().labelOf(edge));
            }

            output.println();
        });
        List<RelationshipEdge<? extends RelationshipType>> edges = edgeTypedDiff.getNonNestingEdges();
        for(RelationshipEdge<? extends  RelationshipType> edge : edges){
            if (edge.getType() == Implication.class){
                output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edge.getFrom().getID(), edge.getTo().getID(), LineGraphConstants.LG_IMPLEDGE) + StringUtils.LINEBREAK);
            }else if(edge.getType() == Alternative.class){
                output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edge.getFrom().getID(), edge.getTo().getID(), LineGraphConstants.LG_ALTEDGE) + StringUtils.LINEBREAK);
            }
        }

    }

    /**
     * Returns debug metadata that was recorded during export.
     */
    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }
}
