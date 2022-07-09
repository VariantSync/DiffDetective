package org.variantsync.diffdetective.diff.difftree.traverse;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;
import org.variantsync.diffdetective.diff.difftree.transform.Duplication;

import java.util.HashMap;

public class EqualsTraversal implements DiffTreeVisitor {

    private HashMap<Integer, DiffNode> duplicatedNodes;
    private boolean hasAllNodes;


    public boolean compareTrees(DiffTree aTree, DiffTree bTree) {
        return compareTrees(aTree.getRoot(), bTree.getRoot());
    }

    public boolean compareTrees(DiffNode aTree, DiffNode bTree) {
        HashMap<Integer, DiffNode> aMap = generateHashmap(aTree);
        HashMap<Integer, DiffNode> bMap = generateHashmap(bTree);
        return aMap.equals(bMap);
    }

    public HashMap<Integer, DiffNode> generateHashmap(DiffTree diffTree) {
        return generateHashmap(diffTree.getRoot());
    }

    public HashMap<Integer, DiffNode> generateHashmap(DiffNode subtree) {
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
     * Compare
     *
     * @param traversal
     * @param subtree
     */
    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        // Generate nodes
        if (!this.hasAllNodes) {
            this.duplicatedNodes.put(subtree.getID(), Duplication.shallowClone(subtree));
            for (final DiffNode child : subtree.getAllChildren()) {
                this.duplicatedNodes.put(child.getID(), Duplication.shallowClone(child));
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
}
