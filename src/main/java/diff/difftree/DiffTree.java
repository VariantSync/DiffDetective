package diff.difftree;

import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static util.Functional.when;

/**
 * Implementation of the diff tree.
 */
public class DiffTree {
    private final DiffNode root;
    private DiffTreeSource source;

    public DiffTree(DiffNode root) {
        this(root, DiffTreeSource.Unknown);
    }

    public DiffTree(DiffNode root, DiffTreeSource source) {
        this.root = root;
        this.source = source;
    }

    public static DiffTree fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws IOException {
        final String fullDiff = IO.readAsString(p);
        final DiffTree t = DiffTreeParser.createDiffTree(fullDiff, collapseMultipleCodeLines, ignoreEmptyLines);
        if (t != null) {
            t.setSource(new PatchFile(p));
        }
        return t;
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

    public List<DiffNode> computeAllNodesThat(final Predicate<DiffNode> property) {
        final List<DiffNode> nodes = new ArrayList<>();
        forAll(when(property, nodes::add));
        return nodes;
    }

    public List<DiffNode> computeCodeNodes() {
        return computeAllNodesThat(DiffNode::isCode);
    }

    public List<DiffNode> computeAnnotationNodes() {
        return computeAllNodesThat(DiffNode::isMacro);
    }

    public List<DiffNode> computeAllNodes() {
        final List<DiffNode> allnodes = new ArrayList<>();
        forAll(allnodes::add);
        return allnodes;
    }

    public void setSource(final DiffTreeSource source) {
        this.source = source;
    }

    public DiffTreeSource getSource() {
        return source;
    }

    public int computeSize() {
        AtomicInteger size = new AtomicInteger();
        forAll(n -> size.incrementAndGet());
        return size.get();
    }

    public boolean isEmpty() {
        return root == null || root.getChildren().isEmpty();
    }

    @Override
    public String toString() {
        return "DiffTree of " + source;
    }
}
