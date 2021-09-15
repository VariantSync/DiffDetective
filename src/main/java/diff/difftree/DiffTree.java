package diff.difftree;

import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import util.IO;

import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of the diff tree.
 * Contains lists of all code nodes and all annotation nodes
 */
public class DiffTree {
    private final DiffNode root;
    private final List<DiffNode> codeNodes;
    private final List<DiffNode> annotationNodes;

    public DiffTree(DiffNode root, List<DiffNode> codeNodes, List<DiffNode> annotationNodes) {
        this.root = root;
        this.codeNodes = codeNodes;
        this.annotationNodes = annotationNodes;
    }

    public static DiffTree fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws IOException {
        final String fullDiff = IO.readAsString(p);
        return DiffTreeParser.createDiffTree(fullDiff, collapseMultipleCodeLines, ignoreEmptyLines);
    }

    public DiffTree forAll(final Consumer<DiffNode> procedure) {
        DiffTreeTraversal.forAll(procedure).visit(this);
        return this;
    }

    public DiffTree traverse(final DiffTreeVisitor visitor) {
        DiffTreeTraversal.with(visitor).visit(this);
        return this;
    }

    public DiffNode getRoot() {
        return root;
    }

    public List<DiffNode> getCodeNodes() {
        return codeNodes;
    }

    public List<DiffNode> getAnnotationNodes() {
        return annotationNodes;
    }

    public int size() {
        return 1 /*Root*/ + codeNodes.size() + annotationNodes.size();
    }

    public boolean isEmpty() {
        return size() == 1;
    }
}
