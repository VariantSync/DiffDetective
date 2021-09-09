package diff.difftree.traverse;

import diff.difftree.DiffNode;
import diff.difftree.DiffTree;

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
        final Integer id = subtree.getID();
        if (!visited.containsKey(id)) {
            visitor.visit(this, subtree);
            visited.put(id, true);
        }
    }

    public void visitChildrenOf(final DiffNode subtree) {
        for (final DiffNode child : subtree.getChildren()) {
            visit(child);
        }
    }
}
