package org.variantsync.diffdetective.diff.difftree;

import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeVisitor;

import java.util.HashMap;

public class Duplication implements DiffTreeVisitor {

    private HashMap<Integer, DiffNode> duplicatedNodes;
    private boolean hasAllNodes;

    /**
     * Duplicates a given node
     *
     * @return A duplication of the input node without parent and child notes
     */
    static public DiffNode shallowClone(DiffNode node) {
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
     * Tree duplication
     */
    public DiffTree deepClone(DiffTree diffTree) {
        return new DiffTree(deepClone(diffTree.getRoot()), diffTree.getSource());
    }

    /**
     * Subtree duplication
     */
    public DiffNode deepClone(DiffNode subtree) {
        this.duplicatedNodes = new HashMap<>();
        this.hasAllNodes = false;
        // fill hashmap
        DiffTreeTraversal.with(this).visit(subtree);
        this.hasAllNodes = true;
        // Add connections
        DiffTreeTraversal.with(this).visit(subtree);
        return this.duplicatedNodes.get(subtree.getID());
    }

    /**
     * Tree duplication which is returned as a hashmap for easier manipulation
     */
    public HashMap<Integer, DiffNode> deepCloneAsHashmap(DiffTree tree) {
        return deepCloneAsHashmap(tree.getRoot());
    }

    /**
     * Subtree duplication which is returned as a hashmap for easier manipulation
     */
    public HashMap<Integer, DiffNode> deepCloneAsHashmap(DiffNode subtree) {
        this.duplicatedNodes = new HashMap<>();
        this.hasAllNodes = false;
        // fill hashmap
        DiffTreeTraversal.with(this).visit(subtree);
        this.hasAllNodes = true;
        // Add connections
        DiffTreeTraversal.with(this).visit(subtree);
        return this.duplicatedNodes;
    }

    /**
     * Create a shallow clone of every node
     *
     * @param traversal
     * @param subtree
     */
    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        // Generate nodes
        if (!this.hasAllNodes) {
            this.duplicatedNodes.put(subtree.getID(), shallowClone(subtree));
            for (final DiffNode child : subtree.getAllChildren()) {
                traversal.visit(child);
            }
            // create connections
        } else {
            for (final DiffNode child : subtree.getAllChildren()) {
                Integer beforeParentId = child.getBeforeParent() != null ? child.getBeforeParent().getID() : null;
                Integer afterParentId = child.getAfterParent() != null ? child.getAfterParent().getID() : null;

                if (this.duplicatedNodes.get(child.getID()).getBeforeParent() != null || this.duplicatedNodes.get(child.getID()).getAfterParent() != null) {
                    continue;
                }

                this.duplicatedNodes.get(child.getID()).addBelow(
                        this.duplicatedNodes.get(beforeParentId),
                        this.duplicatedNodes.get(afterParentId));

                traversal.visit(child);
            }

        }

    }

    public String toString() {
        return "DiffTreeDuplication";
    }
}
