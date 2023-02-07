package org.variantsync.diffdetective.variation.diff;

import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;

public class AtomicDiffComparison {
    private AtomicDiffComparison() {
    }

    /**
     * Checks if both arguments are structurally equal (except for child ordering).
     *
     * Two nodes are considered equal iff their {@link DiffNode#getID IDs} are equal
     * .
     */
    public static boolean equals(DiffTree first, DiffTree second) {
        var firstNodes = new HashMap<Integer, DiffNode>();
        first.forAll(node -> firstNodes.put(node.getID(), node));

        return second.allMatch(secondNode -> {
            DiffNode firstNode = firstNodes.remove(secondNode.getID());
            return firstNode != null && childrenIDs(firstNode).equals(childrenIDs(secondNode));
        }) && firstNodes.isEmpty();
    }

    /**
     * Extract the set of {@link DiffNode#getID IDs} for the direct children of {@code node}.
     */
    private static Set<Integer> childrenIDs(DiffNode node) {
        return node.getAllChildren().stream().map(DiffNode::getID).collect(Collectors.toSet());
    }
}
