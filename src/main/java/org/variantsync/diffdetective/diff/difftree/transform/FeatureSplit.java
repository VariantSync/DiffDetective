package org.variantsync.diffdetective.diff.difftree.transform;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.*;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeatureSplit {
    /**
     * Extracts exactly one feature or feature query from a given difftree
     *
     * @return a Hashmap of features and its corresponding feature-aware DiffTrees
     */
    public static HashMap<String, DiffTree> featureSplit(DiffTree initDiffTree, Node query) {
        ArrayList<Node> queries = new ArrayList<>();
        queries.add(query);
        return featureSplit(initDiffTree, queries);
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
        if (cluster.size() == 1) return cluster.get(0);
        return generateFeatureAwareDiffTree(
                Stream.concat(
                        Stream.of(composeDiffTrees(cluster.get(0), cluster.get(1))),
                        cluster.subList(2, cluster.size()).stream()
                ).collect(Collectors.toList())
        );
    }

    /**
     * Composes two DiffTrees into one feature-aware TreeDiff
     */
    public static DiffTree composeDiffTrees(DiffTree first, DiffTree second) {
        if (second == null && first == null) return null;
        if (first == null || first.isEmpty()) return second;
        if (second == null || second.isEmpty()) return first;

        HashSet<DiffNode> allFirstNodes = new HashSet<>(first.computeAllNodes());
        HashSet<DiffNode> allSecondNodes = new HashSet<>(second.computeAllNodes());
        ArrayList<DiffNode> allNodes = new ArrayList<>();
        allNodes.addAll(allFirstNodes);
        allNodes.addAll(allSecondNodes);

        HashSet<DiffEdge> allEdges = allNodes.stream().map(
                node -> node.getAllChildren().stream().map(
                        child -> new DiffEdge(node, child)
                ).collect(Collectors.toSet())).collect(HashSet::new, Set::addAll, Set::addAll);

        HashSet<DiffNode> composedTree = new HashSet<>();

        allEdges.forEach(edge -> {
            if (!composedTree.contains(edge.parent)) composedTree.add(Duplication.shallowClone(edge.parent));
            if (!composedTree.contains(edge.child)) composedTree.add(Duplication.shallowClone(edge.child));

            DiffNode cpParent = composedTree.stream().filter(node -> node.equals(edge.parent)).findFirst().orElseThrow();
            DiffNode cpChild = composedTree.stream().filter(node -> node.equals(edge.child)).findFirst().orElseThrow();

            // Add all changes, unchanged node edges aren't added here
            if (cpChild.isAdd() || cpChild.isNon() && cpParent.isAdd()) cpParent.addAfterChild(cpChild);
            if (cpChild.isRem() || cpChild.isNon() && cpParent.isRem()) cpParent.addBeforeChild(cpChild);
        });

        DiffNode composeRoot = composedTree.stream().filter(DiffNode::isRoot).findFirst().orElseThrow();
        DiffTree composeTree = new DiffTree(composeRoot, first.getSource());

        allEdges.forEach(edge -> {
            if (!edge.child.isNon()) return;

            DiffNode cpParent = composedTree.stream().filter(node -> node.equals(edge.parent)).findFirst().orElseThrow();
            DiffNode cpChild = composedTree.stream().filter(node -> node.equals(edge.child)).findFirst().orElseThrow();

            if (cpChild.getBeforeParent() == null && cpChild.getAfterParent() != null)
                cpChild.getAfterParent().addBeforeChild(cpChild);
            if (cpChild.getAfterParent() == null && cpChild.getBeforeParent() != null)
                cpChild.getBeforeParent().addAfterChild(cpChild);
            if (cpChild.getBeforeParent() == null && cpChild.getAfterParent() == null)
                cpChild.addBelow(cpParent, cpParent);
        });

        composeTree.assertConsistency();
        return composeTree;
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
    private static Boolean evaluate(DiffTree subtree, Node query) {
        return subtree.anyMatch(node -> (node.getBeforeParent() != null && satSolver(node.getBeforePresenceCondition(), query)) || node.getAfterParent() != null && satSolver(node.getAfterPresenceCondition(), query));
    }

    /**
     * Checks if the query and the presence condition intersect, by checking implication.
     *
     * @param query: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    private static boolean satSolver(Node presenceCondition, Node query) {
        return SAT.implies(presenceCondition, query);
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
        AtomicDiffComparison comparison = new AtomicDiffComparison();
        // check for duplicates
        for (DiffTree tree : allTrees) {
            boolean detected = false;
            for (DiffTree setTree : treeSet) {
                if (comparison.equals(tree, setTree)) detected = true;
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
        List<Integer> includedNodes = subtree.computeAllNodes().stream().map(DiffNode::getID).toList();
        List<HashSet<Integer>> parentNodes = includedNodes.stream()
                .map(elem -> findAncestors(
                                initDiffTree.computeAllNodesThat(inspectedNode -> inspectedNode.getID() == elem).get(0)
                        )
                )
                .toList();

        Set<Integer> allNodes = new HashSet<>(parentNodes.stream().flatMap(Set::stream).toList());

        DiffTree copy = new Duplication().deepClone(initDiffTree);
        List<DiffNode> toDelete = copy.computeAllNodesThat(elem -> !allNodes.contains(elem.getID()));
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
    static public HashSet<Integer> findAncestors(DiffNode node) {
        HashSet<Integer> ancestorNodes = new HashSet<>();
        ancestorNodes.add(node.getID());
        if (node.getBeforeParent() != null) ancestorNodes.addAll(findAncestors(node.getBeforeParent()));
        if (node.getAfterParent() != null) ancestorNodes.addAll(findAncestors(node.getAfterParent()));
        return ancestorNodes;
    }

    public String toString() {
        return "FeatureSplit";
    }
}

