package diff.difftree;

import diff.difftree.parse.DiffTreeParser;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import util.Assert;
import util.IO;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
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

    public boolean allMatch(final Predicate<DiffNode> condition) {
        final AtomicBoolean all = new AtomicBoolean(true);
        DiffTreeTraversal.with((traversal, subtree) -> {
            if (condition.test(subtree)) {
                traversal.visitChildrenOf(subtree);
            } else {
                all.set(false);
            }
        }).visit(this);
        return all.get();
    }

    public boolean anyMatch(final Predicate<DiffNode> condition) {
        final AtomicBoolean matchFound = new AtomicBoolean(false);
        DiffTreeTraversal.with((traversal, subtree) -> {
            if (condition.test(subtree)) {
                matchFound.set(true);
            } else {
                traversal.visitChildrenOf(subtree);
            }
        }).visit(this);
        return matchFound.get();
    }

    public boolean noneMatch(final Predicate<DiffNode> condition) {
        return !anyMatch(condition);
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
        return root == null || root.getCardinality() == 0;
    }

    private static class HasPathToRootCached {
        private final Map<Integer, Boolean> cache = new HashMap<>();

        public boolean hasPathToRoot(final DiffNode d) {
            if (d == null || d.isRoot()) {
                return true;
            }

            final int i = d.getID();
            final Boolean res = cache.getOrDefault(i, null);
            final boolean result;
            if (res != null) {
                result = res;
            } else {
                result = hasPathToRoot(d.getBeforeParent()) && hasPathToRoot(d.getAfterParent());
                cache.put(i, result);
            }

            return result;
        }
    }

    public void assertConsistency() {
        final HasPathToRootCached c = new HasPathToRootCached();
        forAll(n -> {
            n.assertConsistency();
            Assert.assertTrue(c.hasPathToRoot(n), () -> "Not all ancestors of " + n + " are descendants of the root!");
        });
    }

    @Override
    public String toString() {
        return "DiffTree of " + source;
    }
}
