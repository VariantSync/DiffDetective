package org.variantsync.diffdetective.variation.diff.traverse;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * Class for traversing VariationDiffs and accumulating results.
 * VariationDiffs are directed, acyclic graphs but not actually trees.
 * Thus, a recursive traversal of VariationDiffs will yield to many nodes being visited multiple times or
 * even infeasible runtimes when VariationDiffs are very deep.
 *
 * A VariationDiffTraversal guarantees that each node in a VariationDiff is visited at most once.
 * A VariationDiffTraversal does not give any guarantees on the order nodes are visited in but it
 * is a depth-first search like approach.
 * The order in which nodes are visited is customizable with a {@link VariationDiffVisitor} that decides on each
 * visited node, how to proceed the traversal.
 *
 * @author Paul Bittner
 */
public class VariationDiffTraversal<L extends Label> {
    private final Set<DiffNode<L>> visited;
    private final VariationDiffVisitor<L> visitor;

    private VariationDiffTraversal(final VariationDiffVisitor<L> visitor) {
        this.visitor = visitor;
        this.visited = new HashSet<>();
    }

    /**
     * Creates a traversal with the given visitor.
     * @param visitor Visitor that is invoked on each node and always decides how to proceed the traversal.
     * @return The new traversal.
     */
    public static <L extends Label> VariationDiffTraversal<L> with(final VariationDiffVisitor<L> visitor) {
        return new VariationDiffTraversal<>(visitor);
    }

    /**
     * Creates a new traversal that will invoke the given callback once for each node in a visited VariationDiff.
     * @param procedure Callback that is invoked exactly once on each DiffNode in a VariationDiff.
     * @return The new traversal that will visit each node exactly once.
     */
    public static <L extends Label> VariationDiffTraversal<L> forAll(final Consumer<DiffNode<L>> procedure) {
        return with((traversal, subtree) -> {
            procedure.accept(subtree);
            traversal.visitChildrenOf(subtree);
        });
    }

    /**
     * Start the traversal of the given tree at its root.
     * @param tree The tree to traverse.
     */
    public void visit(final VariationDiff<L> tree) {
        visit(tree.getRoot());
    }

    /**
     * Start the traversal of a VariationDiff at the given DiffNode.
     * @param subtree The node at which to start the traversal.
     */
    public void visit(final DiffNode<L> subtree) {
        if (markAsVisited(subtree)) {
            visitor.visit(this, subtree);
        }
    }

    /**
     * Continues the traversal by visiting all children of the given node sequentially.
     * @param subtree The node whose children to visit.
     */
    public void visitChildrenOf(final DiffNode<L> subtree) {
        for (final DiffNode<L> child : subtree.getAllChildren()) {
            visit(child);
        }
    }

    /**
     * Treat the given node as already visited regardless whether this is actually true or not.
     * @param node Node that should be treated as already visited.
     * @return True if the node was unvisited and is now marked visited.
     *         False if the node was already marked visited.
     */
    private boolean markAsVisited(final DiffNode<L> node) {
        return visited.add(node);
    }
}
