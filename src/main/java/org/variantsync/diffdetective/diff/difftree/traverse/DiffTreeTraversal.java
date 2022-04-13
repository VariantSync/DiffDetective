package org.variantsync.diffdetective.diff.difftree.traverse;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.HashMap;
import java.util.function.Consumer;

public class DiffTreeTraversal {
    private final HashMap<Integer, Boolean> visited;
    private final DiffTreeVisitor visitor;

    private DiffTreeTraversal(final DiffTreeVisitor visitor) {
        this.visitor = visitor;
        this.visited = new HashMap<>();
    }

    public static DiffTreeTraversal with(final DiffTreeVisitor visitor) {
        return new DiffTreeTraversal(visitor);
    }

    public static DiffTreeTraversal forAll(final Consumer<DiffNode> procedure) {
        return with((traversal, subtree) -> {
            procedure.accept(subtree);
            traversal.visitChildrenOf(subtree);
        });
    }

    public void visit(final DiffTree tree) {
        visit(tree.getRoot());
    }

    public void visit(final DiffNode subtree) {
        if (markAsVisited(subtree)) {
            visitor.visit(this, subtree);
        }
    }

    public void visitChildrenOf(final DiffNode subtree) {
        for (final DiffNode child : subtree.getAllChildren()) {
            visit(child);
        }
    }

    /**
     * Treat the given node as already visited regardless whether this is actually true or not.
     * @param node Node that should be treated as already visited.
     * @return True if the node was marked visited. False if the node was already marked visited.
     */
    public boolean markAsVisited(final DiffNode node) {
        final Integer id = node.getID();
        if (!visited.containsKey(id)) {
            visited.put(id, true);
            return true;
        }
        return false;
    }
}
