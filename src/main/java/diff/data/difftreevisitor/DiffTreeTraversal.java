package diff.data.difftreevisitor;

import diff.data.DiffNode;

import java.util.HashMap;

public class DiffTreeTraversal {
    private final HashMap<Integer, Boolean> visited;
    private final DiffTreeVisitor visitor;

    public DiffTreeTraversal(final DiffTreeVisitor visitor) {
        this.visitor = visitor;
        this.visited = new HashMap<>();
    }

    public void visit(final DiffNode subtree) {
        final Integer id = subtree.getID();
        if (!visited.containsKey(id)) {
            visitor.visit(this, subtree);
            visited.put(id, true);
        }
    }
}
