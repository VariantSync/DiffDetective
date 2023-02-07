package org.variantsync.diffdetective.variation.diff.transform;

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.variation.diff.AtomicDiffComparison;
import org.variantsync.diffdetective.variation.diff.DiffEdge;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.DiffTree;
import org.variantsync.diffdetective.variation.diff.Time;

public class FeatureSplit {
    /**
     * Extracts exactly one feature or feature query from a given difftree
     *
     * @return a Hashmap of features and its corresponding feature-aware DiffTrees
     */
    public static HashMap<String, DiffTree> featureSplit(DiffTree initDiffTree, Node query) {
        return featureSplit(initDiffTree, List.of(query));
    }

    /**
     * Allows for extraction of multiple features sequentially
     *
     * @return a Hashmap of features and its corresponding feature-aware DiffTrees
     */
    public static HashMap<String, DiffTree> featureSplit(DiffTree initDiffTree, List<Node> queries) {
        List<DiffTree> subtrees = generateAllSubtrees(initDiffTree);
        HashMap<String, List<DiffTree>> clusters = generateClusters(subtrees, queries);
        return generateFeatureAwareDiffTrees(clusters);
    }

    /**
     * Returns feature-aware DiffTrees based on the given clusters
     *
     * @param clusters contains the grouped subtrees
     * @return a List of subtrees that represent changes to one feature or one composite feature
     */
    public static HashMap<String, DiffTree> generateFeatureAwareDiffTrees(HashMap<String, List<DiffTree>> clusters) {
        HashMap<String, DiffTree> featureAwareTreeDiffs = new HashMap<>();
        for (Map.Entry<String, List<DiffTree>> entry : clusters.entrySet()) {
            featureAwareTreeDiffs.put(entry.getKey(),
                    generateFeatureAwareDiffTree(entry.getValue()));
        }
        return featureAwareTreeDiffs;
    }

    /**
     * Composes all subtrees into one feature-aware DiffTrees
     *
     * @param cluster contains subtrees of a cluster
     * @return a feature-aware DiffTrees
     */
    public static DiffTree generateFeatureAwareDiffTree(List<DiffTree> cluster) {
        if (cluster.size() == 0) return null;

        DiffTree current = cluster.get(0);
        for (var next : cluster.subList(1, cluster.size())) {
            current = composeDiffTrees(current, next);
        }
        return current;
    }

    /**
     * Composes two DiffTrees into one feature-aware TreeDiff
     */
    public static DiffTree composeDiffTrees(DiffTree first, DiffTree second) {
        if (second == null && first == null) return null;
        if (first == null || first.isEmpty()) return second;
        if (second == null || second.isEmpty()) return first;

        HashSet<DiffEdge> allEdges = getAllEdges(first, second);
        HashSet<DiffNode> composedTree = new HashSet<>();
        DiffNode composeRoot = DiffNode.createRoot();
        composedTree.add(composeRoot);

        allEdges.forEach(edge -> {
            if (!composedTree.contains(edge.parent())) composedTree.add(edge.parent().shallowClone());
            if (!composedTree.contains(edge.child())) composedTree.add(edge.child().shallowClone());

            DiffNode cpParent = composedTree.stream().filter(node -> node.equals(edge.parent())).findFirst().orElseThrow();
            DiffNode cpChild = composedTree.stream().filter(node -> node.equals(edge.child())).findFirst().orElseThrow();

            // Add all changes, unchanged node edges aren't added here
            if (cpChild.isAdd() || cpChild.isNon() && cpParent.isAdd()) cpParent.addChild(cpChild, AFTER);
            if (cpChild.isRem() || cpChild.isNon() && cpParent.isRem()) cpParent.addChild(cpChild, BEFORE);
        });

        allEdges.forEach(edge -> {
            if (!edge.child().isNon()) return;

            DiffNode cpParent = composedTree.stream().filter(node -> node.equals(edge.parent())).findFirst().orElseThrow();
            DiffNode cpChild = composedTree.stream().filter(node -> node.equals(edge.child())).findFirst().orElseThrow();

            if (cpChild.getParent(BEFORE) == null && cpChild.getParent(AFTER) != null)
                cpChild.getParent(AFTER).addChild(cpChild, BEFORE);
            if (cpChild.getParent(AFTER) == null && cpChild.getParent(BEFORE) != null)
                cpChild.getParent(BEFORE).addChild(cpChild, AFTER);
            if (cpChild.getParent(BEFORE) == null && cpChild.getParent(AFTER) == null)
                cpChild.addBelow(cpParent, cpParent);
        });

        DiffTree composeTree = new DiffTree(composeRoot, first.getSource());
        composeTree.assertConsistency();
        return composeTree;
    }

    private static HashSet<DiffEdge> getAllEdges(DiffTree first, DiffTree second) {
        HashSet<DiffNode> allFirstNodes = new HashSet<>(first.computeAllNodes());
        HashSet<DiffNode> allSecondNodes = new HashSet<>(second.computeAllNodes());
        ArrayList<DiffNode> allNodes = new ArrayList<>();
        allNodes.addAll(allFirstNodes);
        allNodes.addAll(allSecondNodes);

        HashSet<DiffEdge> allEdges = allNodes.stream().map(
                node -> node.getAllChildren().stream().map(
                        child -> new DiffEdge(node, child)
                ).collect(Collectors.toSet())).collect(HashSet::new, Set::addAll, Set::addAll);
        return allEdges;
    }


    /**
     * Generates a cluster for each given query and a "remaining" cluster for all non-selected subtrees
     *
     * @param queries: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    public static HashMap<String, List<DiffTree>> generateClusters(List<DiffTree> subtrees, List<Node> queries) {
        HashMap<String, List<DiffTree>> clusters = new HashMap<>();
        clusters.put("remains", new ArrayList<>());

        for (DiffTree subtree : subtrees) {
            boolean hasFound = false;
            for (Node query : queries) {
                if (evaluate(subtree, query)) {
                    if (!clusters.containsKey(query.toString())) clusters.put(query.toString(), new ArrayList<>());
                    clusters.get(query.toString()).add(subtree); // call by reference
                    hasFound = true;
                    break;
                }
            }
            if (!hasFound) clusters.get("remains").add(subtree);
        }
        return clusters;
    }

    /**
     * compares every node in the subtree with the query
     *
     * @return true if query is implied in a presence condition of a node
     */
    private static boolean evaluate(DiffTree subtree, Node query) {
        return subtree.anyMatch(node -> satSolver(node, query, BEFORE) || satSolver(node, query, AFTER));
    }

    /**
     * Checks if {@code query} and the presence condition of {@code node} at {@code time}
     * intersect by checking implication.
     *
     * @param node the node whose presence condition at {@code time} to check
     * @param query feature mapping which represents a query.
     * @param time which presence condition to check
     * @return true if {@code query} is implied in the presence condition at {@code time}
     */
    private static boolean satSolver(DiffNode node, Node query, Time time) {
        return node.getParent(time) != null && SAT.implies(node.getPresenceCondition(time), query);
    }

    /**
     * Generates valid subtrees, which represent all changes from the initial DiffTree
     *
     * @param initDiffTree is transformed into subtrees
     * @return a set of subtrees
     */
    public static List<DiffTree> generateAllSubtrees(DiffTree initDiffTree) {
        List<DiffTree> allTrees = initDiffTree.computeAllNodesThat(elem -> !elem.isNon()).stream().map(elem -> generateSubtree(elem, initDiffTree)).toList();
        List<DiffTree> treeSet = new ArrayList<>();
        // check for duplicates
        for (DiffTree tree : allTrees) {
            boolean detected = false;
            for (DiffTree setTree : treeSet) {
                if (AtomicDiffComparison.equals(tree, setTree)) {
                    detected = true;
                    break;
                }
            }
            if (!detected) treeSet.add(tree);
        }

        return treeSet;
    }

    /**
     * Return a minimal and valid subtree of the initial DiffTree, which contains the given node
     *
     * @param node         that has to be included in the subtree
     * @param initDiffTree the DiffTree, where the subtree is based of
     * @return subtree of initDiffTree, which contains only nodes linked to node
     */
    static public DiffTree generateSubtree(DiffNode node, DiffTree initDiffTree) {
        DiffTree subtree = new DiffTree(node, initDiffTree.getSource());
        Set<Integer> includedNodes = subtree
            .computeAllNodes()
            .stream()
            .flatMap(elem -> findAncestors(elem).stream())
            .map(DiffNode::getID)
            .collect(Collectors.toSet());

        DiffTree copy = initDiffTree.deepClone();
        List<DiffNode> toDelete = copy.computeAllNodesThat(elem -> !includedNodes.contains(elem.getID()));
        toDelete.forEach(DiffNode::drop);
        copy.assertConsistency();
        return copy;
    }

    /**
     * returns a list of parent nodes, which are linked between the root node and the given node
     *
     * @param node a child node of a DiffTree
     * @return a list of parent ids
     */
    static public HashSet<DiffNode> findAncestors(DiffNode node) {
        var ancestorNodes = new HashSet<DiffNode>();
        ancestorNodes.add(node);
        Time.forAll(time -> {
            if (node.getParent(time) != null) {
                ancestorNodes.addAll(findAncestors(node.getParent(time)));
            }
        });
        return ancestorNodes;
    }

    public String toString() {
        return "FeatureSplit";
    }
}

