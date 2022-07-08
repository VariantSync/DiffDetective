package org.variantsync.diffdetective.diff.difftree.serialize;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.util.StringUtils;

/**
 * Exporter that converts a single DiffTree's nodes and edges to linegraph.
 */
public class DiffTreeLineGraphExporter {
    private final StringBuilder nodesString = new StringBuilder();
    private final StringBuilder edgesString = new StringBuilder();

    private final DiffTree diffTree;

    private final DiffTreeSerializeDebugData debugData;

    /**
     * Creates a new exporter that will export the given tree.
     */
    public DiffTreeLineGraphExporter(DiffTree treeToExport) {
        this.diffTree = treeToExport;
        this.debugData = new DiffTreeSerializeDebugData();
    }

    /**
     * Converts the given node and its edges to linegraph using the formats specified in the given options.
     * The produced linegraph statements will be added to the internal StringBuilders.
     * @param node The node to convert to linegraph format together with its edges.
     * @param options Options that specify the node and edge format to use.
     */
    private void visit(DiffNode node, DiffTreeLineGraphExportOptions options) {
        switch (node.diffType) {
            case ADD -> ++debugData.numExportedAddNodes;
            case REM -> ++debugData.numExportedRemNodes;
            case NON -> ++debugData.numExportedNonNodes;
        }

        nodesString
                .append(options.nodeFormat().toLineGraphLine(node))
                .append(StringUtils.LINEBREAK);

        edgesString
                .append(options.edgeFormat().getParentEdgeLines(node));
    }

    /**
     * Export this exporter's tree using the given options.
     * This method will return the final linegraph as string.
     * The string will contain all linegraph statements for the tree's nodes and edges,
     * but not the tree header.
     * @param options Options that specify the node and edge format to use.
     * @return The linegraph as String.
     * @see LineGraphExport#composeTreeInLineGraph
     */
    public String export(DiffTreeLineGraphExportOptions options) {
        diffTree.forAll(n -> visit(n, options));
        final String result = nodesString.toString() + edgesString;
        StringUtils.clear(nodesString);
        StringUtils.clear(edgesString);
        return result;
    }

    /**
     * Returns debug metadata that was recorded during export.
     */
    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }
}
