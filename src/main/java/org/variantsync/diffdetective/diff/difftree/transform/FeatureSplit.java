package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.Duplication;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FeatureSplit {

    /**
     * Returns feature-aware DiffTrees based on the changes in the input DiffTree
     *
     * @param initDiffTree contains all changes
     * @param Feature feature which is checked for occurrence
     * @return a List of subtrees that represent changes to one feature or one composite feature
     */
    public List<DiffTree> generateFeatureAwareDiffTrees(DiffTree initDiffTree, String Feature){
        HashSet<DiffTree> subtrees = generateAllSubtrees(initDiffTree);
        //TODO: Add methods to combine subtrees to feature-aware commits

        return null;
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
