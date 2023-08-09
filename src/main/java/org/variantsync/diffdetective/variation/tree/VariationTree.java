package org.variantsync.diffdetective.variation.tree;

import org.variantsync.diffdetective.datasets.PatchDiffParseOptions;
import org.variantsync.diffdetective.diff.result.DiffParseException;
import org.variantsync.diffdetective.util.Assert;
import org.variantsync.diffdetective.variation.DiffLinesLabel;
import org.variantsync.diffdetective.variation.Label;
import org.variantsync.diffdetective.variation.VariationLabel;
import org.variantsync.diffdetective.variation.diff.DiffNode;
import org.variantsync.diffdetective.variation.diff.VariationDiff;
import org.variantsync.diffdetective.variation.diff.Projection;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParseOptions;
import org.variantsync.diffdetective.variation.diff.parse.VariationDiffParser;
import org.variantsync.diffdetective.variation.diff.source.FromVariationTreeSource;
import org.variantsync.diffdetective.variation.tree.source.LocalFileSource;
import org.variantsync.diffdetective.variation.tree.source.VariationTreeSource;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import static org.variantsync.diffdetective.variation.diff.Time.BEFORE;

/**
 * Representation of a concrete variation tree with source information.
 *
 * @param root the root of the variation tree
 * @param source from which source code the variation tree was obtained
 * @param <L> The type of label stored in this tree.
 *
 * @see VariationTreeNode
 * @author Benjamin Moosherr
 */
public record VariationTree<L extends Label>(
    ConcreteTreeNode<VariationLabel<L>> root,
    VariationTreeSource source
) {
    /** Creates a {@code VariationTree} with the given root and an unknown source. */
    public VariationTree(ConcreteTreeNode<VariationLabel<L>> root) {
        this(root, VariationTreeSource.Unknown);
    }

    /** Creates a {@code VariationTree} with the given root and source. */
    public VariationTree(ConcreteTreeNode<VariationLabel<L>> root, VariationTreeSource source) {
        this.root = root;
        this.source = source;

        Assert.assertTrue(root.isRoot());
    }

    /**
     * Same as {@link #fromFile(BufferedReader, VariationTreeSource, VariationDiffParseOptions)}
     * but registers {@code path} as source.
     */
    public static VariationTree<DiffLinesLabel> fromFile(
        final Path path,
        final VariationDiffParseOptions parseOptions
    ) throws IOException, DiffParseException {
        try (BufferedReader file = Files.newBufferedReader(path)) {
            return fromFile(
                file,
                new LocalFileSource(path),
                    parseOptions
            );
        }
    }

    /**
     * Parses a {@code VariationTree} from source code with C preprocessor annotations.
     *
     * @param input the source code to be parsed
     * @param parseOptions {@link PatchDiffParseOptions} for the parsing process.
     * @return a new {@code VariationTree} representing {@code input}
     * @throws IOException if {@code input} throws {@code IOException}
     * @throws DiffParseException if some preprocessor annotations can't be parsed
     */
    public static VariationTree<DiffLinesLabel> fromFile(
            final BufferedReader input,
            final VariationTreeSource source,
            final VariationDiffParseOptions parseOptions
            ) throws IOException, DiffParseException {
        ConcreteTreeNode<VariationLabel<DiffLinesLabel>> tree = VariationDiffParser
            .createVariationTree(input, parseOptions)
            .getRoot()
            // Arbitrarily choose the BEFORE projection as both should be equal.
            .projection(BEFORE)
            .deepCopy();

        return new VariationTree<>(tree, source);
    }

    public static <L extends Label> VariationTree<L> fromProjection(final Projection<L> projection, final VariationTreeSource source) {
        return fromVariationNode(projection, source);
    }

    public static <L extends Label, T extends TreeNode<T, VariationLabel<L>>> VariationTree<L> fromVariationNode(final T node, final VariationTreeSource source) {
        return new VariationTree<>(
                node.deepCopy(),
                source
        );
    }

    public VariationDiff<L> toVariationDiff(final Function<ConcreteTreeNode<VariationLabel<L>>, DiffNode<L>> nodeConverter) {
        return new VariationDiff<>(
                DiffNode.unchanged(nodeConverter, root()),
                new FromVariationTreeSource(source())
        );
    }

    public VariationDiff<L> toCompletelyUnchangedVariationDiff() {
        return toVariationDiff(DiffNode::unchangedFlat);
    }

    /**
     * Invokes the given callback for each node in this Variation Tree in depth-first order.
     * @param action callback
     * @return this
     */
    public VariationTree<L> forAllPreorder(final Consumer<ConcreteTreeNode<VariationLabel<L>>> action) {
        root.forAllPreorder(action);
        return this;
    }

    /**
     * Checks whether any node in this tree satisfies the given condition.
     * The condition might not be invoked on every node in case a node is found.
     * @param condition A condition to check on each node.
     * @return True iff the given condition returns true for at least one node in this tree.
     */
    public boolean anyMatch(final Predicate<ConcreteTreeNode<VariationLabel<L>>> condition) {
        return root().anyMatch(condition);
    }

    /**
     * Returns true iff this tree contains the given node.
     * Containment check is done on referential and not on textual
     * equality (i.e., nodes are compared using ==).
     * @param node The node to check for containment.
     */
    public boolean contains(ConcreteTreeNode<VariationLabel<L>> node) {
        return anyMatch(n -> n == node);
    }

    /**
     * Returns the number of nodes in this Variation Tree.
     */
    public int computeSize() {
        AtomicInteger size = new AtomicInteger();
        forAllPreorder(n -> size.incrementAndGet());
        return size.get();
    }

    public VariationTree<L> deepCopy() {
        return deepCopy(new HashMap<>());
    }

    /**
     * Creates a deep copy of this variation tree.
     *
     * <p>The map {@code oldToNew} should be empty as it will be filled by this method. After the
     * method call, the map keys will contain all nodes in this tree. The corresponding values will
     * be the nodes in the returned tree, where each pair (k, v) denotes that v was cloned from k.
     *
     * @param oldToNew A map that memorizes the translation of individual nodes.
     * @return A deep copy of this tree.
     */
    public VariationTree<L> deepCopy(final Map<ConcreteTreeNode<VariationLabel<L>>, ConcreteTreeNode<VariationLabel<L>>> oldToNew) {
        return new VariationTree<>(root.deepCopy(oldToNew), this.source);
    }

    @Override
    public String toString() {
        return "variation tree from " + source;
    }
}
