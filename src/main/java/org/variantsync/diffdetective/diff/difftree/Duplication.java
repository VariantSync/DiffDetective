package org.variantsync.diffdetective.diff.difftree;

import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;

import java.util.HashMap;

public class Duplication {
    private Duplication() {
    }

    /**
     * Duplicates the given node.
     * No properties of {@link DiffNode} are cloned, instead they are shared with {@code node}.
     *
     * @return a new instance of {@link DiffNode} without parent and child nodes
     */
    public static DiffNode shallowClone(DiffNode node) {
        return new DiffNode(node.diffType, node.nodeType, node.getFromLine(), node.getToLine(), node.getDirectFeatureMapping(), node.getLines());
    }

    /**
     * Recursively duplicates the given tree.
     * All properties except for children and parents are shared between {@code diffTree} and the
     * result.
     *
     * @return a new instance of {@link DiffTree} with the same structure as {@code diffTree} but
     * different {@link DiffNode} instances
     */
    public static DiffTree deepClone(DiffTree diffTree) {
        return new DiffTree(deepClone(diffTree.getRoot()), diffTree.getSource());
    }

    /**
     * Recursively duplicates the given tree.
     * All properties except for children and parents are shared between {@code tree} and the
     * result.
     *
     * @return a new instance of {@link DiffNode} with the same structure as {@code tree} but
     * different {@link DiffNode} instances
     */
    public static DiffNode deepClone(DiffNode tree) {
        var duplicatedNodes = new HashMap<DiffNode, DiffNode>();

        DiffTreeTraversal.with((traversal, subtree) -> {
            final DiffNode duplicatedSubtree = shallowClone(subtree);
            duplicatedNodes.put(subtree, duplicatedSubtree);

            for (var child : subtree.getAllChildren()) {
                traversal.visit(child);
                var duplicatedChild = duplicatedNodes.get(child);

                if (subtree.isBeforeChild(child)) {
                    duplicatedSubtree.addBeforeChild(duplicatedChild);
                }

                if (subtree.isAfterChild(child)) {
                    duplicatedSubtree.addAfterChild(duplicatedChild);
                }
            }
        }).visit(tree);

        return duplicatedNodes.get(tree);
    }
}
