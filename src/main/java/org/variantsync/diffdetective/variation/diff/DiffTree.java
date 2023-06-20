package org.variantsync.diffdetective.variation.diff;

import com.github.gumtreediff.matchers.MappingStore;
import com.github.gumtreediff.matchers.Matcher;
import com.github.gumtreediff.matchers.Matchers;
import com.github.gumtreediff.tree.Tree;

import org.tinylog.Logger;
import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.datasets.Repository;
import org.variantsync.diffdetective.diff.git.CommitDiff;
import org.variantsync.diffdetective.diff.git.GitDiffer;
import org.variantsync.diffdetective.diff.git.PatchDiff;
import org.variantsync.diffdetective.diff.git.PatchReference;
import org.variantsync.diffdetective.diff.result.CommitDiffResult;
import org.variantsync.diffdetective.diff.result.DiffError;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.diff.text.DiffLineNumber;
import org.variantsync.diffdetective.gumtree.WrappedDiffTree;
import org.variantsync.diffdetective.gumtree.WrappedVariationTree;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.DiffTreeParser;
import org.variantsync.diffdetective.variation.diff.source.DiffTreeSource;
import org.variantsync.diffdetective.variation.diff.source.PatchFile;
import org.variantsync.diffdetective.variation.diff.source.PatchString;
import org.variantsync.diffdetective.variation.diff.source.VariationTreeDiffSource;
import org.variantsync.diffdetective.variation.diff.traverse.DiffTreeTraversal;
import org.variantsync.diffdetective.variation.diff.traverse.DiffTreeVisitor;
import org.variantsync.diffdetective.variation.tree.VariationNode;
import org.variantsync.diffdetective.variation.tree.VariationTree;
import org.variantsync.functjonal.Result;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;

import static org.variantsync.diffdetective.variation.diff.DiffType.ADD;
import static org.variantsync.diffdetective.variation.diff.DiffType.NON;
import static org.variantsync.diffdetective.variation.diff.DiffType.REM;
import static org.variantsync.diffdetective.variation.diff.Time.AFTER;
import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;
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
     * Parses a DiffTree from the given file.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param p Path to a diff file.
     * @param parseOptions {@link DiffTreeParseOptions} for the parsing process.
     * @return A result either containing the parsed DiffTree or an error message in case of failure.
     * @throws IOException when the given file could not be read for some reason.
     */
    public static DiffTree fromFile(final Path p, DiffTreeParseOptions parseOptions) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(p)) {
            final DiffTree tree = DiffTreeParser.createDiffTree(file, parseOptions);
            tree.setSource(new PatchFile(p));
            return tree;
        }
    }

    /**
     * Parses a DiffTree from the given unix diff.
     * The file should contain a text-based diff without any meta information.
     * So just lines preceded by "+", "-", or " " are expected.
     * @param diff The diff as text. Lines should be separated by a newline character. Each line should be preceded by either "+", "-", or " ".
     * @param parseOptions {@link DiffTreeParseOptions} for the parsing process.
     * @return A result either containing the parsed DiffTree or an error message in case of failure.
     * @throws DiffParseException if {@code diff} couldn't be parsed
     */
    public static DiffTree fromDiff(final String diff, final DiffTreeParseOptions parseOptions) throws DiffParseException {
        final DiffTree tree = DiffTreeParser.createDiffTree(diff, parseOptions);
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
     * Obtain the DiffNode with the given id in this DiffTree.
     * @param id The id of the node to search.
     * @return The node with the given id if existing, null otherwise.
     */
    public DiffNode getNodeWithID(int id) {
        final DiffNode[] d = {null};

        traverse((traversal, subtree) -> {
            if (subtree.getID() == id) {
                d[0] = subtree;
            } else {
                for (final DiffNode child : subtree.getAllChildren()) {
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
        return root == null || root.isLeaf();
    }

    /**
     * Removes the given node from the DiffTree but keeps its children.
     * The children are moved up, meaning that they are located below the parent of the given
     * node afterwards.
     * @param node The node to remove. Cannot be the root.
     */
    public void removeNode(DiffNode node) {
        Assert.assertTrue(node != root);

        Time.forAll(time -> {
            final DiffNode parent = node.getParent(time);
            if (parent != null) {
                parent.removeChild(node, time);
                parent.addChildren(node.removeChildren(time), time);
            }
        });
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
        private final Map<DiffNode, VisitStatus> cache = new HashMap<>();
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

            return switch (cache.getOrDefault(d, VisitStatus.STRANGER)) {
                case STRANGER -> {
                    // The stranger is now known.
                    cache.putIfAbsent(d, VisitStatus.VISITED);

                    final DiffNode b = d.getParent(BEFORE);
                    final DiffNode a = d.getParent(AFTER);
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

    public boolean isSameAs(DiffTree b) {
        var visited = new HashSet<DiffNode>();
        return isSameAs(this.getRoot(), b.getRoot(), visited);
    }

    private static boolean isSameAs(DiffNode a, DiffNode b, Set<DiffNode> visited) {
        if (!visited.add(a)) {
            return true;
        }

        if (!(
                a.getDiffType().equals(b.getDiffType()) &&
                a.getNodeType().equals(b.getNodeType()) &&
                a.getFromLine().equals(b.getFromLine()) &&
                a.getToLine().equals(b.getToLine()) &&
                (a.getFormula() == null ? b.getFormula() == null : a.getFormula().equals(b.getFormula())) &&
                a.getLabel().equals(b.getLabel())
        )) {
            return false;
        }

        Iterator<DiffNode> aIt = a.getAllChildren().iterator();
        Iterator<DiffNode> bIt = b.getAllChildren().iterator();
        while (aIt.hasNext() && bIt.hasNext()) {
            if (!isSameAs(aIt.next(), bIt.next(), visited)) {
                return false;
            }
        }

        return aIt.hasNext() == bIt.hasNext();
    }

    @Override
    public String toString() {
        return "DiffTree of " + source;
    }

    /**
     * Create a {@link DiffTree} by matching nodes between {@code before} and {@code after} with the
     * default GumTree matcher.
     *
     * @see compareUsingMatching(VariationNode, VariationNode, Matcher)
     */
    public static DiffTree compareUsingMatching(VariationTree before, VariationTree after) {
        DiffNode root = compareUsingMatching(
            before.root(),
            after.root(),
            Matchers.getInstance().getMatcher()
        );

        return new DiffTree(root, new VariationTreeDiffSource(before.source(), after.source()));
    }

    /**
     * Create a {@link DiffNode} by matching nodes between {@code before} and {@code after} with
     * {@code matcher}. The arguments of this function aren't modified (note the
     * {@link compareUsingMatching(DiffNode, VariationNode, Matcher) overload} which modifies
     * {@code before} in-place.
     *
     * @param before the variation tree before an edit
     * @param after the variation tree after an edit
     * @see compareUsingMatching(DiffNode, VariationNode, Matcher)
     */
    public static <A extends VariationNode<A>, B extends VariationNode<B>> DiffNode compareUsingMatching(
        VariationNode<A> before,
        VariationNode<B> after,
        Matcher matcher
    ) {
        return compareUsingMatching(DiffNode.unchanged(before), after, matcher);
    }

    /**
     * Create a {@link DiffNode} by matching nodes between {@code before} and {@code after} with
     * {@code matcher}. The result of this function is {@code before} which is modified in-place. In
     * contrast, {@code after} is kept in tact.
     *
     * Note: There are currently no guarantees about the line numbers. But it is guaranteed that
     * {@link DiffNode#getID} is unique.
     *
     * @param before the variation tree before an edit
     * @param after the variation tree after an edit
     */
    public static <B extends VariationNode<B>> DiffNode compareUsingMatching(
        DiffNode before,
        VariationNode<B> after,
        Matcher matcher
    ) {
        var src = new WrappedDiffTree(before, BEFORE);
        var dst = new WrappedVariationTree(after);

        MappingStore matching = matcher.match(src, dst);
        Assert.assertTrue(matching.has(src, dst));

        removeUnmapped(matching, src);
        for (var child : dst.getChildren()) {
            addUnmapped(matching, src.getDiffNode(), (WrappedVariationTree)child);
        }

        int[] currentID = new int[1];
        DiffTreeTraversal.forAll((node) -> {
            node.setFromLine(node.getFromLine().withLineNumberInDiff(currentID[0]));
            node.setToLine(node.getToLine().withLineNumberInDiff(currentID[0]));
            ++currentID[0];
        }).visit(before);

        return before;
    }

    private static void removeUnmapped(MappingStore mappings, WrappedDiffTree root) {
        for (var node : root.preOrder()) {
            Tree dst = mappings.getDstForSrc(node);
            if (dst == null || !dst.getLabel().equals(node.getLabel())) {
                var diffNode = ((WrappedDiffTree)node).getDiffNode();
                diffNode.diffType = REM;
                diffNode.drop(AFTER);
            }
        }
    }

    private static void addUnmapped(MappingStore mappings, DiffNode parent, WrappedVariationTree afterNode) {
        VariationNode<?> variationNode = afterNode.getVariationNode();
        DiffNode diffNode;

        Tree src = mappings.getSrcForDst(afterNode);
        if (src == null || !src.getLabel().equals(afterNode.getLabel())) {
            int from = variationNode.getLineRange().fromInclusive();
            int to = variationNode.getLineRange().toExclusive();

            diffNode = new DiffNode(
                ADD,
                variationNode.getNodeType(),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, from, from),
                new DiffLineNumber(DiffLineNumber.InvalidLineNumber, to, to),
                variationNode.getFormula(),
                variationNode.getLabelLines()
            );
        } else {
            diffNode = ((WrappedDiffTree)src).getDiffNode();
            if (diffNode.getParent(AFTER) != null) {
                diffNode.drop(AFTER);
            }
        }
        parent.addChild(diffNode, AFTER);

        diffNode.removeChildren(AFTER);
        for (var child : afterNode.getChildren()) {
            addUnmapped(mappings, diffNode, (WrappedVariationTree)child);
        }
    }

    /**
     * Run {@code matcher} on the implicit matching of this variation diff and update this variation
     * tree in-place to reflect the new matching.
     */
    public void improveMatching(Matcher matcher) {
        improveMatching(getRoot(), matcher);
    }

    /**
     * Run {@code matcher} on the matching extracted from {@code tree} and modify {@code tree}
     * in-place to reflect the new matching.
     */
    public static DiffNode improveMatching(DiffNode tree, Matcher matcher) {
        var src = new WrappedDiffTree(tree, BEFORE);
        var dst = new WrappedDiffTree(tree, AFTER);

        MappingStore matching = new MappingStore(src, dst);
        extractMatching(src, dst, matching);
        matcher.match(src, dst, matching);
        Assert.assertTrue(matching.has(src, dst));

        for (var srcNode : src.preOrder()) {
            var dstNode = matching.getDstForSrc(srcNode);
            var beforeNode = ((WrappedDiffTree)srcNode).getDiffNode();
            if (dstNode == null || !srcNode.getLabel().equals(dstNode.getLabel())) {
                if (beforeNode.isNon()) {
                    splitNode(beforeNode);
                }

                Assert.assertTrue(beforeNode.isRem());
            } else {
                var afterNode = ((WrappedDiffTree)dstNode).getDiffNode();

                if (beforeNode != afterNode) {
                    if (beforeNode.isNon()) {
                        splitNode(beforeNode);
                    }
                    if (afterNode.isNon()) {
                        afterNode = splitNode(afterNode);
                    }

                    joinNode(beforeNode, afterNode);
                }

                Assert.assertTrue(beforeNode.isNon());
            }
            beforeNode.assertConsistency();
        }

        return tree;
    }

    private static DiffNode splitNode(DiffNode beforeNode) {
        Assert.assertTrue(beforeNode.isNon());

        DiffNode afterNode = beforeNode.shallowCopy();

        afterNode.diffType = ADD;
        beforeNode.diffType = REM;

        afterNode.addChildren(beforeNode.removeChildren(AFTER), AFTER);
        var afterParent = beforeNode.getParent(AFTER);
        afterParent.insertChild(afterNode, afterParent.indexOfChild(beforeNode, AFTER), AFTER);
        beforeNode.drop(AFTER);

        beforeNode.assertConsistency();
        afterNode.assertConsistency();

        return afterNode;
    }

    private static void joinNode(DiffNode beforeNode, DiffNode afterNode) {
        Assert.assertTrue(beforeNode.isRem());
        Assert.assertTrue(afterNode.isAdd());

        beforeNode.diffType = NON;

        beforeNode.addChildren(afterNode.removeChildren(AFTER), AFTER);

        var afterParent = afterNode.getParent(AFTER);
        afterParent.insertChild(beforeNode, afterParent.indexOfChild(afterNode, AFTER), AFTER);
        afterNode.drop(AFTER);
    }

    private static void extractMatching(
        WrappedDiffTree src,
        WrappedDiffTree dst,
        MappingStore result
    ) {
        Map<DiffNode, Tree> matching = new HashMap<>();

        for (var srcNode : src.preOrder()) {
            DiffNode diffNode = ((WrappedDiffTree)srcNode).getDiffNode();
            if (diffNode.isNon()) {
                matching.put(diffNode, srcNode);
            }
        }

        for (var dstNode : dst.preOrder()) {
            DiffNode diffNode = ((WrappedDiffTree)dstNode).getDiffNode();
            if (diffNode.isNon()) {
                Assert.assertTrue(matching.get(diffNode) != null);
                result.addMapping(matching.get(diffNode), dstNode);
            }
        }
    }

    public DiffTree deepCopy() {
        return new DiffTree(getRoot().deepCopy(), getSource());
    }
}
