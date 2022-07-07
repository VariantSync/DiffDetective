package org.variantsync.diffdetective.diff.difftree.transform;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

public class Duplication {

    /**
     * Tree duplication
     */
    public DiffTree duplicateDiffTree(DiffTree diffTree){
        return deepClone(diffTree.getRoot());
    }

    /**
     * Duplicates a given node
     * @return A duplication of the input node without parent and child notes
     */
    public DiffNode shallowClone(DiffNode node){
        //diffNode.isMultilineMacro
        // diffNode.diffType
        // diffNode.codeType
        // from.equals(diffNode.from)
        // to.equals(diffNode.to)
        // Objects.equals(featureMapping, diffNode.featureMapping)
        // lines.equals(diffNode.lines);
        return new DiffNode(node.diffType, node.codeType, node.getFromLine(), node.getToLine(), node.getDirectFeatureMapping(), node.getLines());
    }

    /**
     * Subtree duplication
     */
    public DiffTree deepClone(DiffNode subtree){
        DiffTree TreeDup = new DiffTree(DiffNode.createRoot());
        return null;
    }



    public String toString() {
        return "DiffTreeDuplication";
    }
}
