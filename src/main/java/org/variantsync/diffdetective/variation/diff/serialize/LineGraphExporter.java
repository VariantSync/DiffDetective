package org.variantsync.diffdetective.variation.diff.serialize;

import java.io.OutputStream;
import java.io.PrintStream;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.functjonal.Functjonal;

/**
 * Exporter that converts a single VariationDiff's nodes and edges to linegraph.
 */
public class LineGraphExporter<L extends Label> implements Exporter<L> {
    private final Format<? super L> format;
    private final VariationDiffSerializeDebugData debugData;

    public LineGraphExporter(Format<? super L> format) {
        this.format = format;
        this.debugData = new VariationDiffSerializeDebugData();
    }

    public LineGraphExporter(LineGraphExportOptions<? super L> options) {
        this(new Format<L>(options.nodeFormat(), options.edgeFormat()));
    }

    /**
     * Export a line graph of {@code variationDiff} into {@code destination}.
     *
     * @param variationDiff to be exported
     * @param destination where the result should be written
     */
    @Override
    public <La extends L> void exportVariationDiff(VariationDiff<La> variationDiff, OutputStream destination) {
        var output = new PrintStream(destination);
        format.forEachNode(variationDiff, (node) -> {
            switch (node.diffType) {
                case ADD -> ++debugData.numExportedAddNodes;
                case REM -> ++debugData.numExportedRemNodes;
                case NON -> ++debugData.numExportedNonNodes;
            }

            output.println(LineGraphConstants.LG_NODE + " " + node.getID() + " " + format.getNodeFormat().toLabel(node));
        });

        format.forEachEdge(variationDiff, edge -> {
            output.print(Functjonal.unwords(LineGraphConstants.LG_EDGE, edge.from().getID(), edge.to().getID(), ""));
            output.print(edge.style().lineGraphType());
            output.print(format.getEdgeFormat().labelOf(edge));
            output.println();
        });
    }

    /**
     * Returns debug metadata that was recorded during export.
     */
    public VariationDiffSerializeDebugData getDebugData() {
        return debugData;
    }
}
