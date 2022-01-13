package diff.difftree;

import diff.difftree.parse.DiffTreeParser;
import diff.difftree.source.PatchFile;
import diff.difftree.source.PatchString;
import diff.difftree.traverse.DiffTreeTraversal;
import diff.difftree.traverse.DiffTreeVisitor;
import diff.result.DiffResult;
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

import static de.variantsync.functjonal.Functjonal.when;

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

    public static DiffResult<DiffTree> fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws IOException {
        final String fullDiff = IO.readAsString(p);
        final DiffResult<DiffTree> tree = DiffTreeParser.createDiffTree(fullDiff, collapseMultipleCodeLines, ignoreEmptyLines);
        tree.unwrap().ifSuccess(t -> t.setSource(new PatchFile(p)));
        return tree;
    }

    public static DiffResult<DiffTree> fromDiff(final String diff, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) {
        final DiffResult<DiffTree> tree = DiffTreeParser.createDiffTree(diff, collapseMultipleCodeLines, ignoreEmptyLines);
        tree.unwrap().ifSuccess(t -> t.setSource(new PatchString(diff)));
        return tree;
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
                for (final DiffNode child : subtree.getAllChildren()) {
                    traversal.visit(child);
                    if (!all.get()) break;
                }
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
                for (final DiffNode child : subtree.getAllChildren()) {
                    traversal.visit(child);
                    if (matchFound.get()) break;
                }
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
        forAll(when(property, (Consumer<? super DiffNode>) nodes::add));
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

    /**
     * @param nodesToCount A condition that returns true for each node that should be counted.
     * @return The number of nodes in this tree that satisfy the given condition.
     */
    public int count(final Predicate<DiffNode> nodesToCount) {
        final AtomicInteger count = new AtomicInteger();
        forAll(d -> {
            if (nodesToCount.test(d)) {
                count.incrementAndGet();
            }
        });
        return count.get();
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

    private static class AllPathsEndAtRoot {
        private final Map<Integer, Boolean> cache = new HashMap<>();
        private final DiffNode root;

        private AllPathsEndAtRoot(final DiffNode root) {
            this.root = root;
        }

        public boolean hasPathToRoot(final DiffNode d) {
            if (d == root) {
                return true;
            }

            final int i = d.getID();
            final Boolean res = cache.getOrDefault(i, null);
            boolean result = true;
            if (res != null) {
                result = res;
            } else {
                final DiffNode b = d.getBeforeParent();
                final DiffNode a = d.getAfterParent();
                if (a == null && b == null) {
                    // We found a second root node which is invalid.
                    return false;
                }
                if (b != null) {
                    result &= hasPathToRoot(b);
                }
                if (a != null) {
                    result &= hasPathToRoot(a);
                }
                cache.put(i, result);
            }

            return result;
        }
    }

    public void assertConsistency() {
        final AllPathsEndAtRoot c = new AllPathsEndAtRoot(root);
        forAll(n -> {
            n.assertConsistency();
            Assert.assertTrue(c.hasPathToRoot(n), () -> "Not all ancestors of " + n + " are descendants of the root!");
        });
    }

    public ConsistencyResult isConsistent() {
        try {
            assertConsistency();
        } catch (AssertionError e) {
            return ConsistencyResult.Failure(e);
        }

        return ConsistencyResult.Success();
    }

    @Override
    public String toString() {
        return "DiffTree of " + source;
    }
}
