package diff.difftree.serialize;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;
import util.StringUtils;

public class DiffTreeLineGraphExporter {
    private final StringBuilder nodesString = new StringBuilder();
    private final StringBuilder edgesString = new StringBuilder();

    private final DiffTree diffTree;

    private final DiffTreeSerializeDebugData debugData;

    public DiffTreeLineGraphExporter(DiffTree treeToExport) {
        this.diffTree = treeToExport;
        this.debugData = new DiffTreeSerializeDebugData();
    }

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

    public String export(DiffTreeLineGraphExportOptions options) {
        diffTree.forAll(n -> visit(n, options));
        final String result = nodesString.toString() + edgesString;
        StringUtils.clear(nodesString);
        StringUtils.clear(edgesString);
        return result;
    }

    public DiffTreeSerializeDebugData getDebugData() {
        return debugData;
    }

}
