package org.variantsync.diffdetective.diff.difftree.transform;

import org.prop4j.Node;
import org.variantsync.diffdetective.analysis.logic.SAT;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.Duplication;
import org.variantsync.diffdetective.feature.PropositionalFormulaParser;

import java.util.*;

public class FeatureSplit {

    public List<DiffTree> featureSplit(DiffTree initDiffTree, List<String> queries){
        HashSet<DiffTree> subtrees = generateAllSubtrees(initDiffTree);
        HashMap<String, List<DiffTree>> clusters = generateClusters(subtrees, queries);
        return generateFeatureAwareDiffTrees(clusters);
    }

    /**
     * Returns feature-aware DiffTrees based on the given clusters
     *
     * @param clusters contains the grouped subtrees
     * @return a List of subtrees that represent changes to one feature or one composite feature
     */
    public List<DiffTree> generateFeatureAwareDiffTrees(HashMap<String, List<DiffTree>> clusters){
        //TODO: Add methods to combine subtrees to feature-aware commits

        return null;
    }

    /**
     * Generates a cluster for each given query and a "remaining" cluster for all non-selected subtrees
     * @param query: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    public HashMap<String, List<DiffTree>> generateClusters(HashSet<DiffTree> subtrees, List<String> queries){
        HashMap<String, List<DiffTree>> clusters = new HashMap<>();
        clusters.put("remains", new ArrayList<>());

        for (DiffTree subtree: subtrees){
            boolean hasFound = false;
            for (String query: queries){
                if (evaluate(subtree, query)){
                    if(clusters.containsKey(query)) clusters.put(query, new ArrayList<>());
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
    private Boolean evaluate(DiffTree subtree, String query){
        return subtree.anyMatch(node -> node.isCode() && (satSolver(node.getAfterPresenceCondition(), query) || satSolver(node.getAfterPresenceCondition(), query)));
    }

    /**
     * Checks if the query and the presence condition intersect, by checking implication.
     * @param query: feature mapping which represents a query.
     * @return true if query is implied in the presence condition
     */
    private boolean satSolver(Node presenceCondition, String query) {
        return SAT.implies(presenceCondition, PropositionalFormulaParser.Default.parse(query));
    }

    /**
     * Generates valid subtrees, which represent all changes from the initial DiffTree
     * @param initDiffTree is transformed into subtrees
     * @return a set of subtrees
     */
    public HashSet<DiffTree> generateAllSubtrees(DiffTree initDiffTree){
        List<DiffTree> subtreeList= initDiffTree.computeAllNodesThat(elem -> !elem.isNon()).stream().map(elem -> generateSubtree(elem, initDiffTree)).toList();
        return new HashSet<>(subtreeList);
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
        List<List<Integer>> parentNodes = includedNodes.stream()
                .map(elem -> findParent(
                        initDiffTree.computeAllNodesThat(inspectedNode -> inspectedNode.getID() == elem).get(0)
                        )
                    )
                .toList();

        Set<Integer> allNodes = new HashSet<>();
        allNodes.addAll(includedNodes);
        allNodes.addAll(parentNodes.stream().flatMap(List::stream).toList());

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
    static public List<Integer> findParent(DiffNode node) {
        ArrayList<Integer> parentNodes = new ArrayList<>();
        parentNodes.add(node.getID());
        if (node.getBeforeParent() != null) {
            parentNodes.addAll(findParent(node.getBeforeParent()));
        }
        if (node.getAfterParent() != null) {
            parentNodes.addAll(findParent(node.getAfterParent()));
        }
        return parentNodes;
    }

    public String toString() {
        return "FeatureSplit";
    }
}
