package diff.difftree.serialize;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import diff.difftree.serialize.nodelabel.DiffTreeNodeLabelFormat;
import diff.difftree.serialize.nodelabel.MiningDiffNodeLineGraphImporter;
import diff.serialize.DiffTreeSerializeDebugData;
import diff.serialize.LineGraphExport;
import util.StringUtils;

public class DiffTreeLineGraphExporter {
    public final static String BEFORE_PARENT = "b";
    public final static String AFTER_PARENT = "a";
    public final static String BEFORE_AND_AFTER_PARENT = "ba";

    private final StringBuilder nodesString = new StringBuilder();
    private final StringBuilder edgesString = new StringBuilder();

    private final DiffTree diffTree;
    
    /**
     * The format in which the nodes are printed to the line graph.
     */
    private final DiffTreeNodeLabelFormat nodeLabelFormatter;

    private final DiffTreeSerializeDebugData debugData;

    public DiffTreeLineGraphExporter(DiffTree treeToExport) {
        this.diffTree = treeToExport;
        this.debugData = new DiffTreeSerializeDebugData();
        nodeLabelFormatter = new MiningDiffNodeLineGraphImporter(); // TODO correct assignment
    }

    private void visit(DiffNode node, LineGraphExport.Options options) {
        switch (node.diffType) {
            case ADD -> ++debugData.numExportedAddNodes;
            case REM -> ++debugData.numExportedRemNodes;
            case NON -> ++debugData.numExportedNonNodes;
        }

        final int nodeId = node.getID();
        nodesString
                .append(nodeLabelFormatter.writeNodeToLineGraph(node))
                .append(StringUtils.LINEBREAK);

        final DiffNode beforeParent = node.getBeforeParent();
        final DiffNode afterParent = node.getAfterParent();
        final boolean hasBeforeParent = beforeParent != null;
        final boolean hasAfterParent = afterParent != null;

        // If the node has exactly one parent
        if (hasBeforeParent && hasAfterParent && beforeParent == afterParent) {
            edgesString
                    .append(edgeToLineGraph(nodeId, beforeParent.getID(), BEFORE_AND_AFTER_PARENT))
                    .append(StringUtils.LINEBREAK);
        } else {
            if (hasBeforeParent) {
                edgesString
                        .append(edgeToLineGraph(nodeId, beforeParent.getID(), BEFORE_PARENT))
                        .append(StringUtils.LINEBREAK);
            }
            if (hasAfterParent) {
                edgesString
                        .append(edgeToLineGraph(nodeId, afterParent.getID(), AFTER_PARENT))
                        .append(StringUtils.LINEBREAK);
            }
        }
    }

    public String export(LineGraphExport.Options options) {
        diffTree.forAll(n -> visit(n, options));
        final String result = nodesString.toString() + edgesString;
        StringUtils.clear(nodesString);
        StringUtils.clear(edgesString);
        return result;
    }

    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }

    private static String edgeToLineGraph(int fromNodeId, int toNodeId, final String name) {
        return "e " + fromNodeId + " " + toNodeId + " " + name;
    }
}
