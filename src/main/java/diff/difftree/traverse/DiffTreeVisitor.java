package diff.difftree.traverse;

import diff.difftree.DiffNode;

public interface DiffTreeVisitor {
     void visit(DiffTreeTraversal traversal, DiffNode subtree);
}
