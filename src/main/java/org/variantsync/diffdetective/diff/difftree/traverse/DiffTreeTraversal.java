package org.variantsync.diffdetective.diff.difftree.traverse;

import org.variantsync.diffdetective.diff.difftree.DiffNode;
import org.variantsync.diffdetective.diff.difftree.DiffTree;

import java.util.HashMap;
import java.util.function.Consumer;

/**
 * Class for traversing DiffTrees and accumulating results.
 * DiffTrees are directed, acyclic graphs but not actually trees.
 * Thus, a recursive traversal of DiffTrees will yield to many nodes being visited multiple times or
 * even infeasible runtimes when DiffTrees are very deep.
 *
 * A DiffTreeTraversal guarantees that each node in a DiffTree is visited at most once.
 * A DiffTreeTraversal does not give any guarantees on the order nodes are visited in but it
 * is a depth-first search like approach.
 * The order in which nodes are visited is customizable with a {@link DiffTreeVisitor} that decides on each
 * visited node, how to proceed the traversal.
 *
 * @author Paul Bittner
 */
public class DiffTreeTraversal {
    private final HashMap<Integer, Boolean> visited;
    private final DiffTreeVisitor visitor;

    private DiffTreeTraversal(final DiffTreeVisitor visitor) {
        this.visitor = visitor;
        this.visited = new HashMap<>();
    }

    /**
     * Creates a traversal with the given visitor.
     * @param visitor Visitor that is invoked on each node and always decides how to proceed the traversal.
     * @return The new traversal.
     */
    public static DiffTreeTraversal with(final DiffTreeVisitor visitor) {
        return new DiffTreeTraversal(visitor);
    }

    /**
     * Creates a new traversal that will invoke the given callback once for each node in a visited DiffTree.
     * @param procedure Callback that is invoked exactly once on each DiffNode in a DiffTree.
     * @return The new traversal that will visit each node exactly once.
     */
    public static DiffTreeTraversal forAll(final Consumer<DiffNode> procedure) {
        return with((traversal, subtree) -> {
            procedure.accept(subtree);
            traversal.visitChildrenOf(subtree);
        });
    }

    /**
     * Start the traversal of the given tree at its root.
     * @param tree The tree to traverse.
     */
    public void visit(final DiffTree tree) {
        visit(tree.getRoot());
    }

    /**
     * Start the traversal of a DiffTree at the given DiffNode.
     * @param subtree The node at which to start the traversal.
     */
    public void visit(final DiffNode subtree) {
        if (markAsVisited(subtree)) {
            visitor.visit(this, subtree);
        }
    }

    /**
     * Continues the traversal by visiting all children of the given node sequentially.
     * @param subtree The node whose children to visit.
     */
    public void visitChildrenOf(final DiffNode subtree) {
        for (final DiffNode child : subtree.getAllChildren()) {
            visit(child);
        }
    }

    /**
     * Treat the given node as already visited regardless whether this is actually true or not.
     * @param node Node that should be treated as already visited.
     * @return True if the node was unvisited and is now marked visited.
     *         False if the node was already marked visited.
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
