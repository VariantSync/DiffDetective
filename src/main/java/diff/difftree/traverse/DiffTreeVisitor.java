package diff.difftree.traverse;

import diff.difftree.DiffNode;

@FunctionalInterface
public interface DiffTreeVisitor {
     void visit(DiffTreeTraversal traversal, DiffNode subtree);
}
