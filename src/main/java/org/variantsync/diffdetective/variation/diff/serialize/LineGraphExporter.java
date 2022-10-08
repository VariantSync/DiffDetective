package org.variantsync.diffdetective.variation.diff.serialize;

import java.io.OutputStream;
import java.io.PrintStream;

import org.variantsync.diffdetective.variation.diff.DiffTree;
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

        format.forEachEdge(diffTree, edge -> {
            output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edge.from().getID(), edge.to().getID(), ""));
            output.print(edge.style().lineGraphType());
            output.print(format.getEdgeFormat().labelOf(edge));
            output.println();
        });
    }

    /**
     * Returns debug metadata that was recorded during export.
     */
    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }
}
