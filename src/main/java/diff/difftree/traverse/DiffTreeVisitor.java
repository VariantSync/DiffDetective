package diff.difftree.traverse;

import diff.difftree.DiffNode;

@FunctionalInterface
public interface DiffTreeVisitor {
     void visit(final DiffTreeTraversal traversal, final DiffNode subtree);
}
