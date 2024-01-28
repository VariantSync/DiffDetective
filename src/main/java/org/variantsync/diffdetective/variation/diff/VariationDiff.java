package org.variantsync.diffdetective.variation.diff;

import org.apache.commons.io.IOUtils;
import org.eclipse.jgit.diff.DiffAlgorithm;
import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.git.PatchReference;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.diff.construction.GumTreeDiff;
import org.variantsync.diffdetective.variation.diff.construction.JGitDiff;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.source.PatchFile;
import org.variantsync.diffdetective.variation.diff.source.PatchString;
import org.variantsync.diffdetective.variation.diff.source.VariationDiffSource;
import org.variantsync.diffdetective.variation.diff.traverse.VariationDiffTraversal;
import org.variantsync.diffdetective.variation.diff.traverse.VariationDiffVisitor;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.functjonal.Cast;
import org.variantsync.functjonal.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.lang.reflect.Array;
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

import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;
import static org.variantsync.functjonal.Functjonal.when;

/**
 * Implementation of variation tree diffs from our ESEC/FSE'22 paper.
 * An instance of this class represents a variation tree diff. It stores the root of the graph as a {@link DiffNode}.
 * It optionally holds a {@link VariationDiffSource} that describes how the variation tree diff was obtained.
 * The graph structure is implemented by the {@link DiffNode} class.
 *
 * @param <L> The type of label stored in this tree.
 *
 * @author Paul Bittner, Sören Viegener
 */
public class VariationDiff<L extends Label> {
    private final DiffNode<L> root;
    private VariationDiffSource source;

    /**
     * Creates a VariationDiff that only consists of the single given root node.
     * @param root The root of this tree.
     */
    public VariationDiff(DiffNode<L> root) {
        this(root, VariationDiffSource.Unknown);
    }

    /**
     * Creates a VariationDiff that only consists of the single given root node.
     * Remembers the given source as the tree's source of creation.
     * @param root The root of this tree.
     * @param source The data from which the VariationDiff was created.
     */
    public VariationDiff(DiffNode<L> root, VariationDiffSource source) {
        this.root = root;
        this.source = source;
    }

    /**
     * Parses a VariationDiff from the given file.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param p Path to a diff file.
     * @param parseOptions {@link VariationDiffParseOptions} for the parsing process.
     * @return A result either containing the parsed VariationDiff or an error message in case of failure.
     * @throws IOException when the given file could not be read for some reason.
     */
    public static VariationDiff<DiffLinesLabel> fromFile(final Path p, VariationDiffParseOptions parseOptions) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(p)) {
            final VariationDiff<DiffLinesLabel> tree = VariationDiffParser.createVariationDiff(file, parseOptions);
            tree.setSource(new PatchFile(p));
            return tree;
        }
    }

    /**
     * Parses a VariationDiff from the given unix diff.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param diff The diff as text. Lines should be separated by a newline character. Each line should be preceded by either "+", "-", or " ".
     * @param parseOptions {@link VariationDiffParseOptions} for the parsing process.
     * @return A result either containing the parsed VariationDiff or an error message in case of failure.
     * @throws DiffParseException if {@code diff} couldn't be parsed
     */
    public static VariationDiff<DiffLinesLabel> fromDiff(final String diff, final VariationDiffParseOptions parseOptions) throws DiffParseException {
        final VariationDiff<DiffLinesLabel> d;
        try {
            d = VariationDiffParser.createVariationDiff(diff, parseOptions);
        } catch (DiffParseException e) {
            Logger.error("""
                            Could not parse diff:
                            
                            {}
                            """,
                    diff);
            throw e;
        }
        d.setSource(new PatchString(diff));
        return d;
    }

    /**
     * Parses a patch of a Git repository.
     *
     * Warning: The current implementation ignores {@code patchReference.getParentCommitHash}.
     * It assumes that it's equal to the first parent of {@code patchReference.getCommitHash}, so
     * it cannot parse patches across multiple commits.
     *
     * @param patchReference the patch to be parsed
     * @param repository the repository which contains the path {@code patchReference}
     * @return a {@link VariationDiff} representing the referenced patch, or a list of errors
     * encountered while trying to parse the {@link VariationDiff}
     */
    public static Result<VariationDiff<DiffLinesLabel>, List<DiffError>> fromPatch(final PatchReference patchReference, final Repository repository) throws IOException {
        final CommitDiffResult result = new GitDiffer(repository).createCommitDiff(patchReference.getCommitHash());
        final Path changedFile = Path.of(patchReference.getFileName());
        if (result.diff().isPresent()) {
            final CommitDiff commit = result.diff().get();
            for (final PatchDiff patch : commit.getPatchDiffs()) {
                if (changedFile.equals(Path.of(patch.getFileName(AFTER)))) {
                    return Result.Success(patch.getVariationDiff());
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
     * Create a VariationDiff from two given text files.
     * @see #fromLines(String, String, DiffAlgorithm.SupportedAlgorithm, VariationDiffParseOptions)
     */
    public static VariationDiff<DiffLinesLabel> fromFiles(
            final Path beforeFile,
            final Path afterFile,
            DiffAlgorithm.SupportedAlgorithm algorithm,
            VariationDiffParseOptions options)
            throws IOException, DiffParseException 
    {
        try (BufferedReader b = Files.newBufferedReader(beforeFile);
             BufferedReader a = Files.newBufferedReader(afterFile)
        ) {
            return fromLines(IOUtils.toString(b), IOUtils.toString(a), algorithm, options);
        }
    }

    /**
     * Creates a variation diff from to line-based text inputs.
     * This method just forwards to:
     * @see JGitDiff#diff(String, String, DiffAlgorithm.SupportedAlgorithm, VariationDiffParseOptions) 
     */
    public static VariationDiff<DiffLinesLabel> fromLines(
            String before,
            String after,
            DiffAlgorithm.SupportedAlgorithm algorithm,
            VariationDiffParseOptions options) throws IOException, DiffParseException 
    {
        return JGitDiff.diff(before, after, algorithm, options);
    }

    /**
     * Create a {@link VariationDiff} by matching nodes between {@code before} and {@code after} with the
     * default GumTree matcher.
     *
     * @see GumTreeDiff#diffUsingMatching(VariationTree, VariationTree)  
     */
    public static <L extends Label> VariationDiff<L> fromTrees(VariationTree<L> before, VariationTree<L> after) {
        return GumTreeDiff.diffUsingMatching(before, after);
    }

    /**
     * Creates the projection of this variation diff at the given time.
     * The returned value is a deep copy of the variation tree within this diff
     * at the given time.
     * If you instead wish to only have a view on the tree at the given diff
     * have a look at {@link DiffNode#projection(Time)} for this trees {@link #getRoot() root}.
     * @param t The time for which to project the variation tree.
     */
    public VariationTree<L> project(Time t) {
        return VariationTree.fromProjection(
                getRoot().projection(t),
                new ProjectionSource<>(this, t)
        );
    }

    /**
     * Invokes the given callback for each node in this VariationDiff.
     * @param procedure callback
     * @return this
     */
    public VariationDiff<L> forAll(final Consumer<DiffNode<L>> procedure) {
        VariationDiffTraversal.forAll(procedure).visit(this);
        return this;
    }


    /**
     * Traverse this VariationDiff with the given visitor.
     * When visiting a node, the visitor decides how to proceed.
     * @param visitor visitor that is invoked on the root first and then decides how to proceed the traversal.
     * @return this
     */
    public VariationDiff<L> traverse(final VariationDiffVisitor<L> visitor) {
        VariationDiffTraversal.with(visitor).visit(this);
        return this;
    }

    /**
     * Checks whether all nodes in this tree satisfy the given condition.
     * The condition might not be invoked on every node in case it is not necessary.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns true for all nodes in this tree.
     */
    public boolean allMatch(final Predicate<DiffNode<L>> condition) {
        final AtomicBoolean all = new AtomicBoolean(true);
        VariationDiffTraversal.<L>with((traversal, subtree) -> {
            if (condition.test(subtree)) {
                for (final DiffNode<L> child : subtree.getAllChildren()) {
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
    public boolean anyMatch(final Predicate<DiffNode<L>> condition) {
        final AtomicBoolean matchFound = new AtomicBoolean(false);
        VariationDiffTraversal.<L>with((traversal, subtree) -> {
            if (condition.test(subtree)) {
                matchFound.set(true);
            } else {
                for (final DiffNode<L> child : subtree.getAllChildren()) {
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
    public boolean noneMatch(final Predicate<DiffNode<L>> condition) {
        return !anyMatch(condition);
    }

    /**
     * Returns the root node of this tree.
     */
    public DiffNode<L> getRoot() {
        return root;
    }

    /**
     * Obtain the DiffNode with the given id in this VariationDiff.
     * @param id The id of the node to search.
     * @return The node with the given id if existing, null otherwise.
     */
    public DiffNode<L> getNodeWithID(int id) {
        final DiffNode<L>[] d = Cast.unchecked(Array.newInstance(DiffNode.class, 1));

        traverse((traversal, subtree) -> {
            if (subtree.getID() == id) {
                d[0] = subtree;
            } else {
                for (final DiffNode<L> child : subtree.getAllChildren()) {
                    if (d[0] == null) {
                        traversal.visit(child);
                    }
                }
            }
        });

        return d[0];
    }

    /**
     * Returns all nodes that satisfy the given predicate.
     * Traverses the VariationDiff once.
     * @param property Filter for nodes. Should return true if a node should be included.
     * @return A List of all nodes satisfying the given predicate.
     */
    public List<DiffNode<L>> computeAllNodesThat(final Predicate<DiffNode<L>> property) {
        final List<DiffNode<L>> nodes = new ArrayList<>();
        forAll(when(property, (Consumer<? super DiffNode<L>>) nodes::add));
        return nodes;
    }

    /**
     * Returns all artifact nodes of this VariationDiff.
     * @see VariationDiff#computeAllNodesThat
     */
    public List<DiffNode<L>> computeArtifactNodes() {
        return computeAllNodesThat(DiffNode<L>::isArtifact);
    }

    /**
     * Returns all mapping nodes of this VariationDiff.
     * @see VariationDiff#computeAllNodesThat
     */
    public List<DiffNode<L>> computeAnnotationNodes() {
        return computeAllNodesThat(DiffNode<L>::isAnnotation);
    }

    /**
     * Returns all nodes in this VariationDiff.
     * Traverses the VariationDiff once.
     */
    public List<DiffNode<L>> computeAllNodes() {
        final List<DiffNode<L>> allnodes = new ArrayList<>();
        forAll(allnodes::add);
        return allnodes;
    }

    /**
     * Returns the number of nodes in this tree that satisfy the given condition.
     * @param nodesToCount A condition that returns true for each node that should be counted.
     * @return The number of nodes in this tree that satisfy the given condition.
     */
    public int count(final Predicate<DiffNode<L>> nodesToCount) {
        final AtomicInteger count = new AtomicInteger();
        forAll(d -> {
            if (nodesToCount.test(d)) {
                count.incrementAndGet();
            }
        });
        return count.get();
    }

    /**
     * Sets the source of this VariationDiff.
     * @see VariationDiffSource
     */
    public void setSource(final VariationDiffSource source) {
        this.source = source;
    }

    /**
     * Returns the source of this VariationDiff (i.e., the data this VariationDiff was created from).
     * @see VariationDiffSource
     */
    public VariationDiffSource getSource() {
        return source;
    }

    /**
     * Returns the number of nodes in this VariationDiff.
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
        return root == null || root.isLeaf();
    }

    /**
     * Removes the given node from the VariationDiff but keeps its children.
     * The children are moved up, meaning that they are located below the parent of the given
     * node afterwards.
     * @param node The node to remove. Cannot be the root.
     */
    public void removeNode(DiffNode<L> node) {
        Assert.assertTrue(node != root);

        Time.forAll(time -> {
            final DiffNode<L> parent = node.getParent(time);
            if (parent != null) {
                parent.removeChild(node, time);
                parent.addChildren(node.removeChildren(time), time);
            }
        });
    }

    /**
     * Helper class to check for cycles in VariationDiffs.
     * When traversing the tree, an object of this class remembers visited nodes to see
     * if it walks in cycles.
     * Function programmers might think of this as a state monad.
     */
    private static class AllPathsEndAtRoot implements Predicate<DiffNode<?>> {
        private enum VisitStatus {
            STRANGER,
            VISITED,
            ALL_PATHS_END_AT_ROOT,
            NOT_ALL_PATHS_END_AT_ROOT
        }
        private final Map<DiffNode<?>, VisitStatus> cache = new HashMap<>();
        private final DiffNode<?> root;

        private AllPathsEndAtRoot(final DiffNode<?> root) {
            this.root = root;
        }

        /**
         * Returns true if all paths (in parent direction) starting at the given node end at the root.
         */
        @Override
        public boolean test(final DiffNode<?> d) {
            if (d == root) {
                return true;
            }

            return switch (cache.getOrDefault(d, VisitStatus.STRANGER)) {
                case STRANGER -> {
                    // The stranger is now known.
                    cache.putIfAbsent(d, VisitStatus.VISITED);

                    final DiffNode<?> b = d.getParent(BEFORE);
                    final DiffNode<?> a = d.getParent(AFTER);
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
                    cache.put(d, result ? VisitStatus.ALL_PATHS_END_AT_ROOT : VisitStatus.NOT_ALL_PATHS_END_AT_ROOT);
                    yield result;
                }
                // We detected a cycle because we visited a node but did not determine its value yet!
                // Thus, we are stuck in a recursion.
                case VISITED -> true;
                case ALL_PATHS_END_AT_ROOT -> true;
                case NOT_ALL_PATHS_END_AT_ROOT -> false;
            };
        }
    }

    /**
     * Checks whether this VariationDiff is consistent.
     * Throws an error when this VariationDiff is inconsistent (e.g., if it has cycles or an invalid internal state).
     * Has no side-effects otherwise.
     * @see VariationDiff#isConsistent
     */
    public void assertConsistency() {
        final AllPathsEndAtRoot c = new AllPathsEndAtRoot(root);
        forAll(n -> {
            n.assertConsistency();
            Assert.assertTrue(c.test(n), () -> "Not all ancestors of " + n + " are descendants of the root!");
        });
    }

    /**
     * Checks whether this VariationDiff is consistent.
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

    public boolean isSameAs(VariationDiff<L> b) {
        return getRoot().isSameAs(b.getRoot());
    }

    @Override
    public String toString() {
        return "VariationDiff of " + source;
    }

    public VariationDiff<L> deepCopy() {
        return new VariationDiff<>(getRoot().deepCopy(), getSource());
    }
}
