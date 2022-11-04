package org.variantsync.diffdetective.diff.difftree;

import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.CommitDiff;
import org.variantsync.diffdetective.diff.GitDiffer;
import org.variantsync.diffdetective.diff.PatchDiff;
import org.variantsync.diffdetective.diff.PatchReference;
import org.variantsync.diffdetective.diff.difftree.parse.DiffTreeParser;
import org.variantsync.diffdetective.diff.difftree.source.PatchFile;
import org.variantsync.diffdetective.diff.difftree.source.PatchString;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.diff.difftree.traverse.DiffTreeVisitor;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.feature.CPPAnnotationParser;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.functjonal.Result;

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
 * Implementation of variation tree diffs from our ESEC/FSE'22 paper.
 * An instance of this class represents a variation tree diff. It stores the root of the graph as a {@link DiffNode}.
 * It optionally holds a {@link DiffTreeSource} that describes how the variation tree diff was obtained.
 * The graph structure is implemented by the {@link DiffNode} class.
 * @author Paul Bittner, Sören Viegener
 */
public class DiffTree {
    private final DiffNode root;
    private DiffTreeSource source;

    /**
     * Creates a DiffTree that only consists of the single given root node.
     * @param root The root of this tree.
     */
    public DiffTree(DiffNode root) {
        this(root, DiffTreeSource.Unknown);
    }

    /**
     * Creates a DiffTree that only consists of the single given root node.
     * Remembers the given source as the tree's source of creation.
     * @param root The root of this tree.
     * @param source The data from which the DiffTree was created.
     */
    public DiffTree(DiffNode root, DiffTreeSource source) {
        this.root = root;
        this.source = source;
    }

    /**
     * Same as {@link DiffTree#fromFile(Path, boolean, boolean, CPPAnnotationParser)} but with
     * the {@link CPPAnnotationParser#Default default parser} for the lines in the diff.
     */
    public static DiffTree fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws IOException, DiffParseException {
        return fromFile(p, collapseMultipleCodeLines, ignoreEmptyLines, CPPAnnotationParser.Default);
    }

    /**
     * Same as {@link DiffTree#fromDiff(String, boolean, boolean, CPPAnnotationParser)} but with
     * the {@link CPPAnnotationParser#Default default parser} for the lines in the diff.
     *
     * @throws DiffParseException if {@code diff} couldn't be parsed
     */
    public static DiffTree fromDiff(final String diff, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines) throws DiffParseException {
        return fromDiff(diff, collapseMultipleCodeLines, ignoreEmptyLines, CPPAnnotationParser.Default);
    }

    /**
     * Parses a DiffTree from the given file.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param p Path to a diff file.
     * @param collapseMultipleCodeLines Set to true if subsequent lines of source code with
     *                                  the same {@link NodeType type of change} should be
     *                                  collapsed into a single source code node representing
     *                                  all lines at once.
     * @param ignoreEmptyLines Set to true if empty lines should not be included in the DiffTree.
     * @param annotationParser The parser that is used to parse lines in the diff to {@link DiffNode}s.
     * @return A result either containing the parsed DiffTree or an error message in case of failure.
     * @throws IOException when the given file could not be read for some reason.
     */
    public static DiffTree fromFile(final Path p, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines, final CPPAnnotationParser annotationParser) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(p)) {
            final DiffTree tree = DiffTreeParser.createDiffTree(file, collapseMultipleCodeLines, ignoreEmptyLines, annotationParser);
            tree.setSource(new PatchFile(p));
            return tree;
        }
    }

    /**
     * Parses a DiffTree from the given unix diff.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param diff The diff as text. Lines should be separated by a newline character. Each line should be preceded by either "+", "-", or " ".
     * @param collapseMultipleCodeLines Set to true if subsequent lines of source code with
     *                                  the same {@link NodeType type of change} should be
     *                                  collapsed into a single source code node representing
     *                                  all lines at once.
     * @param ignoreEmptyLines Set to true if empty lines should not be included in the DiffTree.
     * @param annotationParser The parser that is used to parse lines in the diff to {@link DiffNode}s.
     * @return A result either containing the parsed DiffTree or an error message in case of failure.
     * @throws DiffParseException if {@code diff} couldn't be parsed
     */
    public static DiffTree fromDiff(final String diff, boolean collapseMultipleCodeLines, boolean ignoreEmptyLines, final CPPAnnotationParser annotationParser) throws DiffParseException {
        final DiffTree tree = DiffTreeParser.createDiffTree(diff, collapseMultipleCodeLines, ignoreEmptyLines, annotationParser);
        tree.setSource(new PatchString(diff));
        return tree;
    }

    /**
     * Parses a patch of a Git repository.
     *
     * Warning: The current implementation ignored {@code patchReference.getParentCommitHash}.
     * It assumes that it's equal to the first parent of {@code patchReference.getCommitHash}, so
     * it cannot parse patches across multiple commits.
     *
     * @param patchReference the patch to be parsed
     * @param repository the repository which contains the path {@code patchReference}
     * @return a {@link DiffTree} representing the referenced patch, or a list of errors
     * encountered while trying to parse the {@link DiffTree}
     */
    public static Result<DiffTree, List<DiffError>> fromPatch(final PatchReference patchReference, final Repository repository) throws IOException {
        final CommitDiffResult result = new GitDiffer(repository).createCommitDiff(patchReference.getCommitHash());
        final Path changedFile = Path.of(patchReference.getFileName());
        if (result.diff().isPresent()) {
            final CommitDiff commit = result.diff().get();
            for (final PatchDiff patch : commit.getPatchDiffs()) {
                if (changedFile.equals(Path.of(patch.getFileName()))) {
                    return Result.Success(patch.getDiffTree());
                }
            }

            Logger.error("There is no patch to "
                        + changedFile
                        + " in the given commit "
                        + patchReference.getCommitHash()
                        + " in repo "
                        + repository.getRepositoryName()
                        + " or it could not be extracted! Reasons are:");

            final List<DiffError> errors = new ArrayList<>(result.errors().size() + 1);
            errors.add(DiffError.FILE_NOT_FOUND);
            errors.addAll(result.errors());
            return Result.Failure(errors);
        }
        return Result.Failure(result.errors());
    }

    /**
     * Invokes the given callback for each node in this DiffTree.
     * @param procedure callback
     * @return this
     */
    public DiffTree forAll(final Consumer<DiffNode> procedure) {
        DiffTreeTraversal.forAll(procedure).visit(this);
        return this;
    }


    /**
     * Traverse this DiffTree with the given visitor.
     * When visiting a node, the visitor decides how to proceed.
     * @param visitor visitor that is invoked on the root first and then decides how to proceed the traversal.
     * @return this
     */
    public DiffTree traverse(final DiffTreeVisitor visitor) {
        DiffTreeTraversal.with(visitor).visit(this);
        return this;
    }

    /**
     * Checks whether all nodes in this tree satisfy the given condition.
     * The condition might not be invoked on every node in case it is not necessary.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns true for all nodes in this tree.
     */
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

    /**
     * Checks whether any node in this tree satisfies the given condition.
     * The condition might not be invoked on every node in case a node is found.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns true for at least one node in this tree.
     */
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

    /**
     * Checks whether no node in this tree satisfies the given condition.
     * The condition might not be invoked on every node.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns false for every node in this tree.
     */
    public boolean noneMatch(final Predicate<DiffNode> condition) {
        return !anyMatch(condition);
    }

    /**
     * Returns the root node of this tree.
     */
    public DiffNode getRoot() {
        return root;
    }

    /**
     * Returns all nodes that satisfy the given predicate.
     * Traverses the DiffTree once.
     * @param property Filter for nodes. Should return true if a node should be included.
     * @return A List of all nodes satisfying the given predicate.
     */
    public List<DiffNode> computeAllNodesThat(final Predicate<DiffNode> property) {
        final List<DiffNode> nodes = new ArrayList<>();
        forAll(when(property, (Consumer<? super DiffNode>) nodes::add));
        return nodes;
    }

    /**
     * Returns all artifact nodes of this DiffTree.
     * @see DiffTree#computeAllNodesThat
     */
    public List<DiffNode> computeArtifactNodes() {
        return computeAllNodesThat(DiffNode::isArtifact);
    }

    /**
     * Returns all mapping nodes of this DiffTree.
     * @see DiffTree#computeAllNodesThat
     */
    public List<DiffNode> computeAnnotationNodes() {
        return computeAllNodesThat(DiffNode::isAnnotation);
    }

    /**
     * Returns all nodes in this DiffTree.
     * Traverses the DiffTree once.
     */
    public List<DiffNode> computeAllNodes() {
        final List<DiffNode> allnodes = new ArrayList<>();
        forAll(allnodes::add);
        return allnodes;
    }

    /**
     * Returns the number of nodes in this tree that satisfy the given condition.
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

    /**
     * Sets the source of this DiffTree.
     * @see DiffTreeSource
     */
    public void setSource(final DiffTreeSource source) {
        this.source = source;
    }

    /**
     * Returns the source of this DiffTree (i.e., the data this DiffTree was created from).
     * @see DiffTreeSource
     */
    public DiffTreeSource getSource() {
        return source;
    }

    /**
     * Returns the number of nodes in this DiffTree.
     */
    public int computeSize() {
        AtomicInteger size = new AtomicInteger();
        forAll(n -> size.incrementAndGet());
        return size.get();
    }

    /**
     * Returns true iff this tree is empty.
     * The tree is considered empty if it only has a root or if it has no nodes at all.
     */
    public boolean isEmpty() {
        return root == null || root.getTotalNumberOfChildren() == 0;
    }

    /**
     * Removes the given node from the DiffTree but keeps its children.
     * The children are moved up, meaning that they are located below the parent of the given
     * node afterwards.
     * @param node The node to remove. Cannot be the root.
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

    /**
     * Unparses this DiffTree into a text-based diff.
     * @return A text-based diff.
     */
    public String toTextDiff() {
        return root.toTextDiff();
    }

    /**
     * Helper class to check for cycles in DiffTrees.
     * When traversing the tree, an object of this class remembers visited nodes to see
     * if it walks in cycles.
     * Function programmers might think of this as a state monad.
     */
    private static class AllPathsEndAtRoot implements Predicate<DiffNode> {
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

        /**
         * Returns true if all paths (in parent direction) starting at the given node end at the root.
         */
        @Override
        public boolean test(final DiffNode d) {
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
                        result &= test(b);
                    }
                    if (a != null) {
                        result &= test(a);
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

    /**
     * Checks whether this DiffTree is consistent.
     * Throws an error when this DiffTree is inconsistent (e.g., if it has cycles or an invalid internal state).
     * Has no side-effects otherwise.
     * @see DiffTree#isConsistent
     */
    public void assertConsistency() {
        final AllPathsEndAtRoot c = new AllPathsEndAtRoot(root);
        forAll(n -> {
            n.assertConsistency();
            Assert.assertTrue(c.test(n), () -> "Not all ancestors of " + n + " are descendants of the root!");
        });
    }

    /**
     * Checks whether this DiffTree is consistent.
     * @return A result that indicates either consistency or inconsistency.
     */
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
