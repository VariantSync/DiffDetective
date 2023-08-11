package org.variantsync.diffdetective.variation.diff.traverse;

import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.DiffNode;

/**
 * Visitor for {@link VariationDiffTraversal}s.
 * A visitor is invoked by the traversal on a node that is currently traversed.
 * The visitor can then perform any computation necessary at the current traversal
 * and then decide how to proceed by again invoking the {@link VariationDiffTraversal#visit} methods
 * of the traversal.
 * @author Paul Bittner
 */
@FunctionalInterface
public interface VariationDiffVisitor<L extends Label> {
     /**
      * Invoked by a traversal when a node is visited.
      * The traversal might be continued by invoking respective methods on the given traversal object again.
      * However, any node that was already visited, will not be visited again.
      * @param traversal The current traversal. May be instructed on how to continue traversal.
      * @param subtree The node that is currently visited.
      * @see VariationDiffTraversal
      */
     void visit(final VariationDiffTraversal<L> traversal, final DiffNode<L> subtree);
}
