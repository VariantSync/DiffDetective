package org.variantsync.diffdetective.diff.difftree;

import org.variantsync.diffdetective.diff.difftree.parse.DiffNodeParser;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.source.PatchFile;
import org.variantsync.diffdetective.diff.difftree.source.PatchString;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeVisitor;
import org.variantsync.diffdetective.diff.result.DiffResult;
import org.variantsync.diffdetective.util.Assert;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.variantsync.functjonal.Functjonal.when;

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
        return fromFile(p, collapseMultipleCodeLines, ignoreEmptyLines, DiffNodeParser.Default);
    }

    public static DiffResult<DiffTree> fromDiff(final String diff, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) {
        return fromDiff(diff, collapseMultipleCodeLines, ignoreEmptyLines, DiffNodeParser.Default);
    }

    public static DiffResult<DiffTree> fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines, final DiffNodeParser annotationParser) throws IOException {
        try (BufferedReader file = Files.newBufferedReader(p)) {
            final DiffResult<DiffTree> tree = DiffTreeParser.createDiffTree(file, collapseMultipleCodeLines, ignoreEmptyLines, annotationParser);
            tree.unwrap().ifSuccess(t -> t.setSource(new PatchFile(p)));
            return tree;
        }
    }

    public static DiffResult<DiffTree> fromDiff(final String diff, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines, final DiffNodeParser annotationParser) {
        final DiffResult<DiffTree> tree = DiffTreeParser.createDiffTree(diff, collapseMultipleCodeLines, ignoreEmptyLines, annotationParser);
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
        return root == null || root.getTotalNumberOfChildren() == 0;
    }

    /**
     * Removes the given node from the DiffTree but keeps its children.
     * @param node The node to remove.
     */
    public void removeNode(DiffNode node) {
        Assert.assertTrue(node != root);

        final DiffNode beforeParent = node.getBeforeParent();
        if (beforeParent != null) {
            beforeParent.removeBeforeChild(node);
            beforeParent.addBeforeChildren(node.removeBeforeChildren());
        }

        final DiffNode afterParent = node.getAfterParent();
        if (afterParent != null) {
            afterParent.removeAfterChild(node);
            afterParent.addAfterChildren(node.removeAfterChildren());
        }
    }

    public String toTextDiff() {
        return root.toTextDiff();
    }

    private static class AllPathsEndAtRoot {
        private enum VisitStatus {
            STRANGER,
            VISITED,
            ALL_PATHS_END_AT_ROOT,
            NOT_ALL_PATHS_END_AT_ROOT
        }
        private final Map<Integer, VisitStatus> cache = new HashMap<>();
        private final DiffNode root;

        private AllPathsEndAtRoot(final DiffNode root) {
            this.root = root;
        }

        public boolean hasPathToRoot(final DiffNode d) {
            if (d == root) {
                return true;
            }

            final int id = d.getID();
            return switch (cache.getOrDefault(id, VisitStatus.STRANGER)) {
                case STRANGER -> {
                    // The stranger is now known.
                    cache.putIfAbsent(id, VisitStatus.VISITED);

                    final DiffNode b = d.getBeforeParent();
                    final DiffNode a = d.getAfterParent();
                    if (a == null && b == null) {
                        // We found a second root node which is invalid.
                        yield false;
                    }

                    boolean result = true;
                    if (b != null) {
                        result &= hasPathToRoot(b);
                    }
                    if (a != null) {
                        result &= hasPathToRoot(a);
                    }

                    // Now we also know the result for the stranger.
                    cache.put(id, result ? VisitStatus.ALL_PATHS_END_AT_ROOT : VisitStatus.NOT_ALL_PATHS_END_AT_ROOT);
                    yield result;
                }
                // We detected a cycle because we visited a node but did not determine its value yet!
                // Thus, we are stuck in a recursion.
                case VISITED -> false;
                case ALL_PATHS_END_AT_ROOT -> true;
                case NOT_ALL_PATHS_END_AT_ROOT -> false;
            };
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
