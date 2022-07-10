package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.Diff;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.Duplication;

import java.util.*;
import java.util.stream.Collectors;

public class FeatureSplit {

    public HashSet<DiffTree> generateAllSubtrees(DiffTree initDiffTree){
        List<DiffTree> subtreeList= initDiffTree.computeAllNodesThat(elem -> !elem.isNon()).stream().map(elem -> generateSubtree(elem, initDiffTree)).toList();
        return new HashSet<DiffTree>(subtreeList);
    }

    /**
     * Return a minimal and valid subtree of the initial DiffTree, which contains the given node
     *
     * @param node         that has to be included in the subtree
     * @param initDiffTree the DiffTree, where the subtree is based of
     * @return subtree of initDiffTree, which contains only nodes linked to node
     */
    public DiffTree generateSubtree(DiffNode node, DiffTree initDiffTree) {
        //HashMap<Integer, DiffNode> copyHashMap = new Duplication().deepCloneAsHashmap(initDiffTree);

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
        toDelete.stream().forEach(DiffNode::drop);
        return copy;
    }

    /**
     * returns a list of parent nodes, which are linked between the root node and the given node
     *
     * @param node a child node of a DiffTree
     * @return a list of parent ids
     */
    public List<Integer> findParent(DiffNode node) {
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
