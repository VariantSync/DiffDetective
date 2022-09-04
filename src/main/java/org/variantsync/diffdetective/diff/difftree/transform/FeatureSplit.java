package org.variantsync.diffdetective.diff.difftree.transform;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffTreeComparison;
import org.variantsync.diffdetective.diff.difftree.Duplication;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;

import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FeatureSplit {
    /**
     * Extracts exactly one feature or feature query from a given difftree
     *
     * @return a Hashmap of features and its corresponding feature-aware DiffTrees
     */
    public static HashMap<String, DiffTree> featureSplit(DiffTree initDiffTree, String query){
        ArrayList<String> queries = new ArrayList<>();
        queries.add(query);
        return featureSplit(initDiffTree, queries);
    }

    /**
     * Allows for extraction of multiple features sequentially
     *
     * @return a Hashmap of features and its corresponding feature-aware DiffTrees
     */
    public static HashMap<String, DiffTree> featureSplit(DiffTree initDiffTree, List<String> queries){
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
    public static HashMap<String, DiffTree> generateFeatureAwareDiffTrees(HashMap<String, List<DiffTree>> clusters){
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
    public static DiffTree generateFeatureAwareDiffTree(List<DiffTree> cluster){
        if (cluster.size() == 0) return null;
        if (cluster.size() == 1) return cluster.get(0);
        return generateFeatureAwareDiffTree(
                Stream.concat(
                    cluster.subList(2, cluster.size()).stream(),
                    Stream.of(composeDiffTrees(cluster.get(0),cluster.get(1)))
                ).collect(Collectors.toList())
        );
    }

    /**
     * Composes two DiffTrees into one feature-aware TreeDiff
     */
    public static DiffTree composeDiffTrees(DiffTree first, DiffTree second){
        if (second == null && first == null) return null;
        if (first == null || first.isEmpty()) return second;
        if (second == null || second.isEmpty()) return first;

        // Get Leaf of subtree
        List<DiffNode> leafNodesFirst = first.computeAllNodesThat(diffNode -> diffNode.getAllChildren().size() == 0);
        // Inspect each leaf
        leafNodesFirst.forEach(leafFirst -> {
                    // Traverse leafNodes Upwards
                    HashSet<Integer> ancestorsFirst = findParent(leafFirst);
                    for (int ancestorFirst : ancestorsFirst){
                        List<DiffNode> ancestorList = first.computeAllNodesThat(node -> node.getID() == ancestorFirst);
                        // do nothing if only root node
                        if(ancestorList.size() == 0) continue;

                        DiffNode ancestorNodeFirst = ancestorList.get(0);

                        // find node, where both DiffTrees start dividing, where only one can exist
                        List<DiffNode> splitSecond = second.computeAllNodesThat(node -> node.getID() == ancestorFirst);
                        List<DiffNode> parentSplitSecond = second.computeAllNodesThat(node ->
                                ancestorNodeFirst.getAfterParent() != null && node.getID() == ancestorNodeFirst.getAfterParent().getID() ||
                                ancestorNodeFirst.getBeforeParent() != null && node.getID() == ancestorNodeFirst.getBeforeParent().getID());

                        if(splitSecond.size() == 0 && parentSplitSecond.size() != 0){
                            // add subtree to new parent
                            DiffNode clonedNode = Duplication.shallowClone(ancestorNodeFirst);
                            clonedNode.stealChildrenOf(ancestorNodeFirst);

                            // first value in parentSplitSecond is beforeParent and second is afterParent
                            if(ancestorNodeFirst.getBeforeParent() == null ){
                                clonedNode.addBelow(null, parentSplitSecond.get(0));
                            } else if (ancestorNodeFirst.getAfterParent() == null) {
                                clonedNode.addBelow(parentSplitSecond.get(0), null);
                            } else {
                                // both parents still can be identical
                                if (parentSplitSecond.size() == 1) {
                                    clonedNode.addBelow(parentSplitSecond.get(0), parentSplitSecond.get(0));
                                } else {
                                    clonedNode.addBelow(parentSplitSecond.get(0), parentSplitSecond.get(1));
                                }
                            }
                            ancestorNodeFirst.drop();
                        }
                    }
                });
        return second;
    }


    /**
     * Generates a cluster for each given query and a "remaining" cluster for all non-selected subtrees
     * @param queries: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    public static HashMap<String, List<DiffTree>> generateClusters(List<DiffTree> subtrees, List<String> queries){
        HashMap<String, List<DiffTree>> clusters = new HashMap<>();
        clusters.put("remains", new ArrayList<>());

        for (DiffTree subtree: subtrees){
            boolean hasFound = false;
            for (String query: queries){
                if (evaluate(subtree, query)){
                    if(!clusters.containsKey(query)) clusters.put(query, new ArrayList<>());
                    clusters.get(query).add(subtree); // call by reference
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
     * @return true if query is implied in a presence condition of a node
     */
    private static Boolean evaluate(DiffTree subtree, String query){
        return subtree.anyMatch(node -> {
            return (node.getBeforeParent() != null && satSolver(node.getBeforePresenceCondition(), query)) || node.getAfterParent() != null && satSolver(node.getAfterPresenceCondition(), query);
        });
    }

    /**
     * Checks if the query and the presence condition intersect, by checking implication.
     * @param query: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    private static boolean satSolver(Node presenceCondition, String query) {
        return SAT.implies(presenceCondition, PropositionalFormulaParser.Default.parse(query));
    }

    /**
     * Generates valid subtrees, which represent all changes from the initial DiffTree
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
                DiffTreeComparison comparison = new DiffTreeComparison();
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
                .map(elem -> findParent(
                        initDiffTree.computeAllNodesThat(inspectedNode -> inspectedNode.getID() == elem).get(0)
                        )
                    )
                .toList();

        Set<Integer> allNodes = new HashSet<>();
        allNodes.addAll(parentNodes.stream().flatMap(Set::stream).toList());

        DiffTree copy = new Duplication().deepClone(initDiffTree);
        List<DiffNode> toDelete = copy.computeAllNodesThat(elem -> !allNodes.contains(elem.getID()));
        toDelete.forEach(DiffNode::drop);
        return copy;
    }

    /**
     * returns a list of parent nodes, which are linked between the root node and the given node
     *
     * @param node a child node of a DiffTree
     * @return a list of parent ids
     */
    static public HashSet<Integer> findParent(DiffNode node) {
        HashSet<Integer> parentNodes = new HashSet<>();
        parentNodes.add(node.getID());
        if (node.getBeforeParent() != null) parentNodes.addAll(findParent(node.getBeforeParent()));
        if (node.getAfterParent() != null) parentNodes.addAll(findParent(node.getAfterParent()));
        return parentNodes;
    }

    public String toString() {
        return "FeatureSplit";
    }
}
