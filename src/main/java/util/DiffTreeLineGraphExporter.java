package util;

import diff.data.DiffNode;
import diff.data.DiffTree;

public class DiffTreeLineGraphExporter {
    public final static String BEFORE_PARENT = "b";
    public final static String AFTER_PARENT = "a";
    public final static String BEFORE_AND_AFTER_PARENT = "ba";

    private final StringBuilder nodesString = new StringBuilder();
    private final StringBuilder edgesString = new StringBuilder();

    private final DiffTree diffTree;

    public DiffTreeLineGraphExporter(DiffTree treeToExport) {
        this.diffTree = treeToExport;
    }

    private void visit(DiffNode node) {
        final int nodeId = node.getID();
        nodesString.append(node.toLineGraphFormat()).append(StringUtils.LINEBREAK);

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

    public String export() {
        visit(diffTree.getRoot());
        for (DiffNode codeNode : diffTree.getCodeNodes()) {
            visit(codeNode);
        }
        for (DiffNode annotationNode : diffTree.getAnnotationNodes()) {
            visit(annotationNode);
        }

        final String result = nodesString.toString() + edgesString;
        StringUtils.clear(nodesString);
        StringUtils.clear(edgesString);
        return result;
    }

    private static String edgeToLineGraph(int fromNodeId, int toNodeId, final String name) {
        return "e " + fromNodeId + " " + toNodeId + " " + name;
    }
}
