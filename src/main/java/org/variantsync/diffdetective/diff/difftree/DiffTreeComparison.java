package org.variantsync.diffdetective.diff.difftree;

import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeVisitor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

public class DiffTreeComparison implements DiffTreeVisitor {
    private HashMap<Integer, List<DiffNode>> comparisonHashMap;

    /**
     * Validate tree equation
     */
    public Boolean equals(DiffTree first, DiffTree second) {
        return first.getSource().equals(second.getSource()) && equals(first.getRoot(), second.getRoot());
    }

    /**
     * validate subtree equation
     */
    public Boolean equals(DiffNode first, DiffNode second) {
        this.comparisonHashMap = new HashMap<>();
        //populate hash map
        DiffTreeTraversal.with(this).visit(first);
        DiffTreeTraversal.with(this).visit(second);

        // compare elements
        return !this.comparisonHashMap.values().stream().map(diffNodes ->
                        diffNodes.size() != 2
                        || !diffNodes.get(0).equals(diffNodes.get(1))
                        || !diffNodes.get(0).getAllChildren().equals(diffNodes.get(1).getAllChildren()))
                .collect(Collectors.toSet()).contains(true);
    }

    /**
     * Traverses subtrees to populate the comparison hash map
     */
    @Override
    public void visit(DiffTreeTraversal traversal, DiffNode subtree) {
        if (!this.comparisonHashMap.containsKey(subtree.getID()))
            this.comparisonHashMap.put(subtree.getID(), new ArrayList<>());
        this.comparisonHashMap.get(subtree.getID()).add(subtree);

        for (final DiffNode child : subtree.getAllChildren()) {
            traversal.visit(child);
        }

    }

    public String toString() {
        return "DiffTreeComparison";
    }
}
