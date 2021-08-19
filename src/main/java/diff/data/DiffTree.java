package diff.data;

import org.pmw.tinylog.Logger;
import util.StringUtils;

import java.util.*;

/**
 * Implementation of the diff tree.
 * Contains lists of all code nodes and all annotation nodes
 */
public class DiffTree {
    private static class DiffTreeToLineGraphExporter {
        final static String BEFORE_PARENT = "BEFORE_PARENT";
        final static String AFTER_PARENT = "AFTER_PARENT";
        final static String BEFORE_AND_AFTER_PARENT = "BEFORE_AND_AFTER_PARENT";

        final Map<DiffNode, Integer> nodeIds = new HashMap<>();
        final StringBuilder nodesString = new StringBuilder();
        final StringBuilder edgesString = new StringBuilder();

        private int nextNodeId = 0;

        private String edgeToLineGraph(int fromNodeId, int toNodeId, final String name) {
            return "e " + fromNodeId + " " + toNodeId + " " + name;
        }

        private void createIDsForNodes(List<DiffNode> nodes) {
            for (final DiffNode node : nodes) {
                nodeIds.put(node, nextNodeId);
                ++nextNodeId;
            }
        }

        private void visit(DiffNode node) {
            final int nodeId = nodeIds.get(node);
            nodesString.append(node.toLineGraphFormat(nodeId)).append(StringUtils.LINEBREAK);

            final DiffNode beforeParent = node.getBeforeParent();
            final DiffNode afterParent = node.getAfterParent();
            final boolean hasBeforeParent = beforeParent != null;
            final boolean hasAfterParent = afterParent != null;

            // If the node has exactly one parent
            if (hasBeforeParent && hasAfterParent && beforeParent == afterParent) {
                edgesString
                        .append(edgeToLineGraph(nodeId, nodeIds.get(beforeParent), BEFORE_AND_AFTER_PARENT))
                        .append(StringUtils.LINEBREAK);
            } else {
                if (hasBeforeParent) {
                    edgesString
                            .append(edgeToLineGraph(nodeId, nodeIds.get(beforeParent), BEFORE_PARENT))
                            .append(StringUtils.LINEBREAK);
                }
                if (hasAfterParent) {
                    edgesString
                            .append(edgeToLineGraph(nodeId, nodeIds.get(afterParent), AFTER_PARENT))
                            .append(StringUtils.LINEBREAK);
                }
            }
        }

        public String export(List<DiffNode> nodes) {
            Logger.info("    Creating IDs");
            createIDsForNodes(nodes);

            Logger.info("    Building Strings");
            for (DiffNode node : nodes) {
                visit(node);
            }

            Logger.info("    Return");
            return nodesString + StringUtils.LINEBREAK + edgesString;
        }
    }

    private final List<DiffNode> codeNodes;
    private final List<DiffNode> annotationNodes;

    private final DiffNode root;

    public DiffTree(DiffNode root, List<DiffNode> codeNodes, List<DiffNode> annotationNodes) {
        this.root = root;
        this.codeNodes = codeNodes;
        this.annotationNodes = annotationNodes;
    }

    public List<DiffNode> getCodeNodes() {
        return codeNodes;
    }

    public List<DiffNode> getAnnotationNodes() {
        return annotationNodes;
    }

    public String toLineGraphFormat() {
        final List<DiffNode> allNodes = new ArrayList<>(1 + codeNodes.size() + annotationNodes.size());
        allNodes.add(root);
        allNodes.addAll(codeNodes);
        allNodes.addAll(annotationNodes);
        return new DiffTreeToLineGraphExporter().export(allNodes);
    }
}
