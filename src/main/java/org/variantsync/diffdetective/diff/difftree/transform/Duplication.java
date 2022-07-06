package org.variantsync.diffdetective.diff.difftree.transform;

import org.prop4j.Node;
import org.variantsync.diffdetective.diff.DiffLineNumber;
import org.variantsync.diffdetective.diff.difftree.CodeType;
import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.DiffType;

public class Duplication {

    /**
     * Tree duplication
     */
    public DiffTree duplicateDiffTree(DiffTree diffTree){
        return duplicateSubTree(diffTree.getRoot());
    }

    /**
     * Subtree duplication
     */
    public DiffTree duplicateSubTree(DiffNode subtree){
        DiffTree TreeDup = new DiffTree(DiffNode.createRoot());
        return null;
    }

    /**
     * Duplicates a given node
     * @return A duplication of the input node without parent and child notes
     */
    public DiffNode duplicateNode(DiffNode node){
        return DiffNode.fromID(node.getID(), node.getLabel());
    }

    public String toString() {
        return "DiffTreeDuplication";
    }
}
