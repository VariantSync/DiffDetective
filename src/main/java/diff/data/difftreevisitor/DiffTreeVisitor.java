package diff.data.difftreevisitor;

import diff.data.DiffNode;

public interface DiffTreeVisitor {
     void visit(DiffTreeTraversal traversal, DiffNode subtree);
}
