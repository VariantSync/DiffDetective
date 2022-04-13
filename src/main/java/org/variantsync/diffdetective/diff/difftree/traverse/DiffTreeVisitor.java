package org.variantsync.diffdetective.diff.difftree.traverse;

import org.variantsync.diffdetective.diff.difftree.DiffNode;

@FunctionalInterface
public interface DiffTreeVisitor {
     void visit(final DiffTreeTraversal traversal, final DiffNode subtree);
}
